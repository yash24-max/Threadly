"""Build the final LLM prompt context from history + RAG passages."""
from __future__ import annotations


def build_messages(
    conversation_history: list[dict[str, str]],
    current_message: str,
    rag_context: str = "",
) -> list[dict[str, str]]:
    """
    Assemble the messages list for LLM API call.
    Keeps last 20 turns, prepends RAG context to user message.
    """
    # Keep last 20 messages to stay within token budget
    history = conversation_history[-20:]

    messages: list[dict[str, str]] = []
    for msg in history:
        messages.append({"role": msg["role"], "content": msg["content"]})

    # Attach RAG context to the current user message
    user_content = current_message
    if rag_context:
        user_content = f"{rag_context}\n\nUser question: {current_message}"

    messages.append({"role": "user", "content": user_content})
    return messages


def build_system_prompt(base_prompt: str, bot_name: str = "") -> str:
    """Render system prompt with bot name substitution."""
    if bot_name and "{{bot.name}}" in base_prompt:
        base_prompt = base_prompt.replace("{{bot.name}}", bot_name)
    return base_prompt
