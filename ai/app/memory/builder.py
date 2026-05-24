"""Conversation memory builder for context assembly."""

import httpx
from typing import Optional

from pydantic import BaseModel, Field

from app.config import settings
from app.logger import get_logger

logger = get_logger(__name__)


class ConversationTurn(BaseModel):
    """Single conversation turn."""

    role: str = Field(..., description="'user' or 'assistant'")
    content: str = Field(..., description="Message content")
    timestamp: str = Field(..., description="ISO timestamp")


class MemoryContext(BaseModel):
    """Assembled conversation context."""

    system_prompt: str = Field(..., description="System prompt for LLM")
    context_text: str = Field(..., description="Full context string")
    recent_turns: list[ConversationTurn] = Field(default_factory=list)
    token_count: int = Field(default=0, description="Estimated tokens")


class MemoryBuilder:
    """Build conversation memory for AI replies."""

    def __init__(self) -> None:
        """Initialize memory builder."""
        self.http_client = httpx.Client(
            base_url=settings.CONVERSATION_SERVICE_URL,
            headers={"X-API-Key": settings.CONVERSATION_API_KEY},
            timeout=30.0,
        )

    def build_context(
        self,
        bot_id: str,
        session_id: str,
        recent_turns: int = 5,
        kb_passages: Optional[list[str]] = None,
    ) -> MemoryContext:
        """Build conversation context for AI reply.

        Args:
            bot_id: Bot/workspace ID
            session_id: Conversation session ID
            recent_turns: Number of recent turns to include
            kb_passages: Optional KB passages to include

        Returns:
            Assembled memory context
        """
        try:
            # Fetch recent conversation turns from conversation service
            turns = self._fetch_conversation_turns(session_id, recent_turns)
            logger.info(f"Fetched {len(turns)} recent turns")

            # Build context string
            context_parts = []

            # Add visitor profile if available
            visitor_info = self._fetch_visitor_info(bot_id, session_id)
            if visitor_info:
                context_parts.append(f"Visitor: {visitor_info}")

            # Add KB passages if provided
            if kb_passages:
                context_parts.append("Knowledge Base Context:")
                for i, passage in enumerate(kb_passages[:3], 1):
                    context_parts.append(f"{i}. {passage}")

            # Add recent conversation history
            if turns:
                context_parts.append("\nRecent Conversation:")
                for turn in turns:
                    role = "Visitor" if turn.role == "user" else "Assistant"
                    context_parts.append(f"{role}: {turn.content}")

            context_text = "\n".join(context_parts)

            # Build system prompt
            system_prompt = self._build_system_prompt(bot_id, context_text)

            # Estimate token count
            token_count = self._estimate_tokens(context_text + system_prompt)

            return MemoryContext(
                system_prompt=system_prompt,
                context_text=context_text,
                recent_turns=turns,
                token_count=token_count,
            )

        except Exception as e:
            logger.error(f"Error building memory context: {e}")
            # Return minimal context on error
            return MemoryContext(
                system_prompt="You are a helpful assistant.",
                context_text="",
                recent_turns=[],
                token_count=0,
            )

    def _fetch_conversation_turns(
        self,
        session_id: str,
        limit: int = 5,
    ) -> list[ConversationTurn]:
        """Fetch recent conversation turns from conversation service.

        Args:
            session_id: Session ID
            limit: Number of turns to fetch

        Returns:
            List of conversation turns
        """
        try:
            response = self.http_client.get(
                f"/api/v1/conversations/{session_id}/turns",
                params={"limit": limit, "reverse": True},
            )
            if response.status_code == 200:
                data = response.json()
                turns = [ConversationTurn(**turn) for turn in data.get("turns", [])]
                return list(reversed(turns))  # Return in chronological order
            logger.warning(f"Failed to fetch turns: {response.status_code}")
            return []
        except Exception as e:
            logger.error(f"Error fetching conversation turns: {e}")
            return []

    def _fetch_visitor_info(self, bot_id: str, session_id: str) -> Optional[str]:
        """Fetch visitor information.

        Args:
            bot_id: Bot ID
            session_id: Session ID

        Returns:
            Visitor info string or None
        """
        try:
            response = self.http_client.get(
                f"/api/v1/bots/{bot_id}/sessions/{session_id}",
            )
            if response.status_code == 200:
                data = response.json()
                session_data = data.get("session", {})
                visitor_name = session_data.get("visitor_name", "Anonymous")
                visitor_email = session_data.get("visitor_email", "")

                if visitor_email:
                    return f"{visitor_name} ({visitor_email})"
                return visitor_name
            return None
        except Exception as e:
            logger.error(f"Error fetching visitor info: {e}")
            return None

    def _build_system_prompt(self, bot_id: str, context: str) -> str:
        """Build system prompt for bot.

        Args:
            bot_id: Bot ID
            context: Conversation context

        Returns:
            System prompt string
        """
        base_prompt = """You are a helpful customer support assistant.

Your responsibilities:
- Answer questions helpfully and accurately
- Be polite and professional
- Provide clear explanations
- Admit when you don't know something
- Ask clarifying questions if needed
- Provide relevant information from the knowledge base when available"""

        if context:
            base_prompt += f"\n\nContext Information:\n{context}"

        return base_prompt

    def _estimate_tokens(self, text: str) -> int:
        """Estimate token count for text.

        Args:
            text: Text to estimate

        Returns:
            Estimated token count
        """
        # Simple heuristic: ~4 characters per token (English)
        return len(text) // 4

    def close(self) -> None:
        """Close HTTP client."""
        try:
            self.http_client.close()
        except Exception as e:
            logger.error(f"Error closing HTTP client: {e}")
