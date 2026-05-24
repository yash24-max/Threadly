'use client';

import { motion } from 'framer-motion';
import { cn } from '@/lib/utils';

/**
 * Generic skeleton component for loading states.
 * Animates a pulse effect to indicate content is loading.
 */
export function Skeleton({
  className,
  animate = true,
  ...props
}: Omit<React.HTMLAttributes<HTMLDivElement>, 'onDrag'> & { animate?: boolean }) {
  return (
    <motion.div
      animate={
        animate
          ? { opacity: [0.5, 0.8, 0.5] }
          : {}
      }
      transition={{ duration: 2, repeat: Infinity }}
      className={cn('bg-slate-200 dark:bg-slate-700 rounded', className)}
      {...(props as any)}
    />
  );
}

/**
 * Skeleton grid for template/integration cards.
 */
export function SkeletonGrid({ count = 8, cols = 4 }: { count?: number; cols?: number }) {
  const colsMap = {
    1: 'grid-cols-1',
    2: 'grid-cols-2',
    3: 'grid-cols-3',
    4: 'grid-cols-4',
    5: 'grid-cols-5',
    6: 'grid-cols-6',
  } as const;

  return (
    <div className={cn('grid gap-4', colsMap[cols as keyof typeof colsMap] ?? 'grid-cols-4')}>
      {[...Array(count)].map((_, i) => (
        <div key={i} className="flex flex-col">
          <Skeleton className="h-40 rounded-t-lg" />
          <div className="space-y-2 p-4 bg-white dark:bg-slate-900 rounded-b-lg">
            <Skeleton className="h-4 w-3/4" />
            <Skeleton className="h-3 w-full" />
            <Skeleton className="h-3 w-2/3" />
            <div className="flex gap-2 pt-2">
              <Skeleton className="h-8 flex-1 rounded" />
              <Skeleton className="h-8 flex-1 rounded" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

/**
 * Skeleton for node panel items.
 */
export function NodePanelSkeleton() {
  return (
    <div className="space-y-3 p-2">
      {[...Array(8)].map((_, i) => (
        <Skeleton key={i} className="h-12 rounded" />
      ))}
    </div>
  );
}

/**
 * Skeleton for template preview modal.
 */
export function TemplatePreviewSkeleton() {
  return (
    <div className="w-full max-w-2xl bg-white dark:bg-slate-900 rounded-lg shadow-2xl overflow-hidden">
      <div className="p-6 space-y-4">
        <div className="flex items-center gap-3">
          <Skeleton className="w-16 h-16 rounded" />
          <div className="flex-1 space-y-2">
            <Skeleton className="h-6 w-2/3" />
            <Skeleton className="h-4 w-full" />
          </div>
        </div>

        <Skeleton className="h-40 rounded" />

        <div className="grid grid-cols-3 gap-4">
          {[...Array(3)].map((_, i) => (
            <Skeleton key={i} className="h-20 rounded" />
          ))}
        </div>
      </div>
    </div>
  );
}

/**
 * Minimal loading spinner for inline states.
 */
export function LoadingSpinner({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) {
  const sizeMap = {
    sm: 'w-4 h-4',
    md: 'w-6 h-6',
    lg: 'w-8 h-8',
  };

  return (
    <motion.div
      animate={{ rotate: 360 }}
      transition={{ duration: 1, repeat: Infinity, ease: 'linear' }}
      className={cn('border-2 border-slate-200 dark:border-slate-700 border-t-blue-600 rounded-full', sizeMap[size])}
    />
  );
}
