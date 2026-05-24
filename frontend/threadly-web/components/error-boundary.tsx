'use client';

import { AlertTriangle, RotateCcw } from 'lucide-react';
import { motion } from 'framer-motion';

interface ErrorBoundaryProps {
  error: Error | null;
  message?: string;
  onRetry?: () => void;
  compact?: boolean;
}

/**
 * Generic error display component for API failures.
 * Shows user-friendly messages and retry button.
 */
export function ErrorBoundary({
  error,
  message,
  onRetry,
  compact = false,
}: ErrorBoundaryProps) {
  if (!error && !message) return null;

  const errorMessage = message || error?.message || 'Something went wrong';

  // Map specific error codes to user-friendly messages
  const getDisplayMessage = () => {
    if (error instanceof Error) {
      if (error.message.includes('404')) {
        return 'Resource not found. Please check your request.';
      }
      if (error.message.includes('403')) {
        return 'You do not have permission to access this resource.';
      }
      if (error.message.includes('500')) {
        return 'Server error. Please try again later.';
      }
      if (error.message.includes('Not authenticated')) {
        return 'Please log in to continue.';
      }
    }
    return errorMessage;
  };

  const displayMessage = getDisplayMessage();

  if (compact) {
    return (
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        className="flex items-center gap-2 px-3 py-2 rounded-lg bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800"
      >
        <AlertTriangle size={16} className="text-red-600 dark:text-red-400 flex-shrink-0" />
        <p className="text-sm text-red-700 dark:text-red-300">{displayMessage}</p>
        {onRetry && (
          <button
            onClick={onRetry}
            className="ml-auto text-xs text-red-600 dark:text-red-400 hover:underline"
          >
            Retry
          </button>
        )}
      </motion.div>
    );
  }

  return (
    <motion.div
      initial={{ opacity: 0, scale: 0.95 }}
      animate={{ opacity: 1, scale: 1 }}
      className="flex flex-col items-center justify-center p-8 rounded-lg bg-red-50 dark:bg-red-900/10 border border-red-200 dark:border-red-800"
    >
      <AlertTriangle size={32} className="text-red-600 dark:text-red-400 mb-3" />
      <p className="text-center text-red-700 dark:text-red-300 font-medium mb-4">
        {displayMessage}
      </p>
      {onRetry && (
        <motion.button
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={onRetry}
          className="flex items-center gap-2 px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white font-medium transition-colors"
        >
          <RotateCcw size={16} />
          Try Again
        </motion.button>
      )}
    </motion.div>
  );
}
