"""LLM provider abstraction and implementations."""

from abc import ABC, abstractmethod
from typing import AsyncIterator, Iterator, Optional

from anthropic import Anthropic
from google.generativeai import GenerativeModel
from openai import OpenAI
from pydantic import BaseModel, Field

from app.config import settings
from app.logger import get_logger

logger = get_logger(__name__)


class LLMResponse(BaseModel):
    """LLM completion response model."""

    text: str = Field(..., description="Generated text content")
    tokens_used: int = Field(default=0, description="Input + output tokens")
    tokens_limit: int = Field(default=0, description="Model token limit")
    cost_usd: float = Field(default=0.0, description="Estimated cost in USD")


class LLMProvider(ABC):
    """Abstract base class for LLM providers."""

    @abstractmethod
    def complete(
        self,
        prompt: str,
        system: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> Iterator[str]:
        """Stream text completion.

        Args:
            prompt: User prompt text
            system: Optional system prompt
            temperature: Sampling temperature (0-1)
            max_tokens: Maximum tokens to generate

        Yields:
            Text tokens from the model
        """
        pass

    @abstractmethod
    async def complete_async(
        self,
        prompt: str,
        system: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> AsyncIterator[str]:
        """Async stream text completion.

        Args:
            prompt: User prompt text
            system: Optional system prompt
            temperature: Sampling temperature (0-1)
            max_tokens: Maximum tokens to generate

        Yields:
            Text tokens from the model
        """
        pass

    @abstractmethod
    def count_tokens(self, text: str) -> int:
        """Count tokens in text.

        Args:
            text: Text to count tokens for

        Returns:
            Number of tokens
        """
        pass

    @abstractmethod
    def classify(
        self,
        text: str,
        categories: list[str],
        system: Optional[str] = None,
    ) -> tuple[str, float]:
        """Classify text into categories.

        Args:
            text: Text to classify
            categories: List of possible categories
            system: Optional system prompt

        Returns:
            Tuple of (category, confidence)
        """
        pass


class AnthropicProvider(LLMProvider):
    """Anthropic Claude provider."""

    def __init__(self) -> None:
        """Initialize Anthropic client."""
        if not settings.ANTHROPIC_API_KEY:
            raise ValueError("ANTHROPIC_API_KEY not configured")
        self.client = Anthropic(api_key=settings.ANTHROPIC_API_KEY)
        self.model = settings.ANTHROPIC_MODEL
        logger.info(f"Initialized Anthropic provider with model: {self.model}")

    def complete(
        self,
        prompt: str,
        system: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> Iterator[str]:
        """Stream text completion using Claude."""
        try:
            with self.client.messages.stream(
                model=self.model,
                max_tokens=max_tokens,
                temperature=temperature,
                system=system or "You are a helpful assistant.",
                messages=[{"role": "user", "content": prompt}],
            ) as stream:
                for text in stream.text_stream:
                    yield text
        except Exception as e:
            logger.error(f"Error in Anthropic completion: {e}")
            raise

    async def complete_async(
        self,
        prompt: str,
        system: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> AsyncIterator[str]:
        """Async stream text completion using Claude."""
        # Anthropic SDK doesn't have async streaming yet, use sync in thread
        for token in self.complete(prompt, system, temperature, max_tokens):
            yield token

    def count_tokens(self, text: str) -> int:
        """Count tokens using Anthropic's API."""
        try:
            response = self.client.messages.count_tokens(
                model=self.model,
                messages=[{"role": "user", "content": text}],
            )
            return response.input_tokens
        except Exception as e:
            logger.error(f"Error counting tokens: {e}")
            # Rough estimate: 1 token ≈ 4 characters
            return len(text) // 4

    def classify(
        self,
        text: str,
        categories: list[str],
        system: Optional[str] = None,
    ) -> tuple[str, float]:
        """Classify text using Claude."""
        prompt = f"""Classify the following text into one of these categories: {', '.join(categories)}

Text: {text}

Respond with ONLY: CATEGORY|CONFIDENCE where CONFIDENCE is 0.0-1.0"""

        try:
            response = self.client.messages.create(
                model=self.model,
                max_tokens=100,
                system=system or "You are a text classification expert.",
                messages=[{"role": "user", "content": prompt}],
            )
            result = response.content[0].text.strip()
            parts = result.split("|")
            if len(parts) == 2:
                return parts[0].strip(), float(parts[1].strip())
            return categories[0], 0.5
        except Exception as e:
            logger.error(f"Error in classification: {e}")
            return categories[0], 0.5


class OpenAIProvider(LLMProvider):
    """OpenAI GPT provider."""

    def __init__(self) -> None:
        """Initialize OpenAI client."""
        if not settings.OPENAI_API_KEY:
            raise ValueError("OPENAI_API_KEY not configured")
        self.client = OpenAI(api_key=settings.OPENAI_API_KEY)
        self.model = settings.OPENAI_MODEL
        logger.info(f"Initialized OpenAI provider with model: {self.model}")

    def complete(
        self,
        prompt: str,
        system: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> Iterator[str]:
        """Stream text completion using GPT."""
        try:
            stream = self.client.chat.completions.create(
                model=self.model,
                max_tokens=max_tokens,
                temperature=temperature,
                stream=True,
                messages=[
                    {"role": "system", "content": system or "You are a helpful assistant."},
                    {"role": "user", "content": prompt},
                ],
            )
            for chunk in stream:
                if chunk.choices[0].delta.content:
                    yield chunk.choices[0].delta.content
        except Exception as e:
            logger.error(f"Error in OpenAI completion: {e}")
            raise

    async def complete_async(
        self,
        prompt: str,
        system: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> AsyncIterator[str]:
        """Async stream text completion using GPT."""
        # OpenAI SDK doesn't have built-in async streaming, use sync
        for token in self.complete(prompt, system, temperature, max_tokens):
            yield token

    def count_tokens(self, text: str) -> int:
        """Estimate tokens for OpenAI."""
        # Rough estimate: 1 token ≈ 4 characters for English
        return len(text) // 4

    def classify(
        self,
        text: str,
        categories: list[str],
        system: Optional[str] = None,
    ) -> tuple[str, float]:
        """Classify text using GPT."""
        prompt = f"""Classify the following text into one of these categories: {', '.join(categories)}

Text: {text}

Respond with ONLY: CATEGORY|CONFIDENCE where CONFIDENCE is 0.0-1.0"""

        try:
            response = self.client.chat.completions.create(
                model=self.model,
                max_tokens=100,
                messages=[
                    {"role": "system", "content": system or "You are a text classification expert."},
                    {"role": "user", "content": prompt},
                ],
            )
            result = response.choices[0].message.content.strip()
            parts = result.split("|")
            if len(parts) == 2:
                return parts[0].strip(), float(parts[1].strip())
            return categories[0], 0.5
        except Exception as e:
            logger.error(f"Error in classification: {e}")
            return categories[0], 0.5


class GeminiProvider(LLMProvider):
    """Google Gemini provider."""

    def __init__(self) -> None:
        """Initialize Gemini client."""
        if not settings.GOOGLE_API_KEY:
            raise ValueError("GOOGLE_API_KEY not configured")
        import google.generativeai as genai

        genai.configure(api_key=settings.GOOGLE_API_KEY)
        self.model = genai.GenerativeModel(settings.GOOGLE_MODEL)
        logger.info(f"Initialized Gemini provider with model: {settings.GOOGLE_MODEL}")

    def complete(
        self,
        prompt: str,
        system: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> Iterator[str]:
        """Stream text completion using Gemini."""
        try:
            full_prompt = f"{system or ''}\n\n{prompt}".strip()
            response = self.model.generate_content(
                full_prompt,
                stream=True,
                generation_config={
                    "temperature": temperature,
                    "max_output_tokens": max_tokens,
                },
            )
            for chunk in response:
                if chunk.text:
                    yield chunk.text
        except Exception as e:
            logger.error(f"Error in Gemini completion: {e}")
            raise

    async def complete_async(
        self,
        prompt: str,
        system: Optional[str] = None,
        temperature: float = 0.7,
        max_tokens: int = 2000,
    ) -> AsyncIterator[str]:
        """Async stream text completion using Gemini."""
        for token in self.complete(prompt, system, temperature, max_tokens):
            yield token

    def count_tokens(self, text: str) -> int:
        """Estimate tokens for Gemini."""
        # Rough estimate: 1 token ≈ 4 characters
        return len(text) // 4

    def classify(
        self,
        text: str,
        categories: list[str],
        system: Optional[str] = None,
    ) -> tuple[str, float]:
        """Classify text using Gemini."""
        prompt = f"""Classify the following text into one of these categories: {', '.join(categories)}

Text: {text}

Respond with ONLY: CATEGORY|CONFIDENCE where CONFIDENCE is 0.0-1.0"""

        try:
            full_prompt = f"{system or ''}\n\n{prompt}".strip()
            response = self.model.generate_content(
                full_prompt,
                generation_config={"max_output_tokens": 100},
            )
            result = response.text.strip()
            parts = result.split("|")
            if len(parts) == 2:
                return parts[0].strip(), float(parts[1].strip())
            return categories[0], 0.5
        except Exception as e:
            logger.error(f"Error in classification: {e}")
            return categories[0], 0.5


def get_provider(provider_name: str) -> LLMProvider:
    """Factory function to get LLM provider instance.

    Args:
        provider_name: Name of provider (anthropic, openai, gemini)

    Returns:
        LLM provider instance

    Raises:
        ValueError: If provider name is invalid
    """
    providers: dict[str, type[LLMProvider]] = {
        "anthropic": AnthropicProvider,
        "openai": OpenAIProvider,
        "gemini": GeminiProvider,
    }

    if provider_name not in providers:
        raise ValueError(
            f"Unknown provider: {provider_name}. Must be one of {list(providers.keys())}"
        )

    return providers[provider_name]()
