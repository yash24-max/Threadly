"""Structured logging configuration."""

import sys
from typing import Any

from loguru import logger

from app.config import settings


def setup_logging() -> None:
    """Configure loguru logger with JSON format."""
    # Remove default handler
    logger.remove()

    # Configure format
    if settings.LOG_FORMAT == "json":
        fmt = (
            "<level>{level: <8}</level> | "
            "<cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> | "
            "<level>{message}</level>"
        )
    else:
        fmt = (
            "<level>{level: <8}</level> | "
            "{time:YYYY-MM-DD HH:mm:ss} | "
            "<cyan>{name}</cyan>:<cyan>{function}</cyan>:<cyan>{line}</cyan> | "
            "<level>{message}</level>"
        )

    # Add handler to stderr
    logger.add(
        sys.stderr,
        format=fmt,
        level=settings.LOG_LEVEL,
        colorize=True,
        backtrace=True,
        diagnose=True,
    )

    # Add file handler for production
    if not settings.DEBUG:
        logger.add(
            "logs/threadly_ai_{time:YYYY-MM-DD}.log",
            format=fmt,
            level="INFO",
            rotation="500 MB",
            retention="7 days",
            compression="zip",
        )


def get_logger(name: str) -> Any:
    """Get logger instance with name binding.

    Args:
        name: Logger name (typically module name)

    Returns:
        Logger instance bound to the name
    """
    return logger.bind(name=name)
