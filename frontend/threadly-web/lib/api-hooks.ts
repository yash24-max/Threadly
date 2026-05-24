'use client';

import { useQuery, useQueryClient } from '@tanstack/react-query';
import { useSession } from 'next-auth/react';
import { api } from './api';
import type { NodeCatalogEntry } from './node-catalog';
import type { Template } from './templates';

export interface Integration {
  id: string;
  name: string;
  category: 'Messaging' | 'CRM' | 'Productivity' | 'Analytics' | 'E-commerce';
  description: string;
  logo?: string;
  color: string;
  connected: boolean;
  actions: string[];
}

export interface NodeCatalogResponse {
  nodes: NodeCatalogEntry[];
  categories: string[];
}

export interface TemplatesResponse {
  templates: Template[];
  categories: string[];
}

export interface IntegrationsResponse {
  integrations: Integration[];
  categories: string[];
}

/**
 * Fetch all node types from the backend.
 * Used in NodePanel component.
 */
export function useNodeCatalog() {
  const { data: session } = useSession();

  return useQuery<NodeCatalogResponse>({
    queryKey: ['node-catalog'],
    queryFn: async () => {
      if (!session?.accessToken) {
        throw new Error('Not authenticated');
      }
      return api.get<NodeCatalogResponse>(
        '/v1/internal/node-catalog',
        session.accessToken as string
      );
    },
    enabled: !!session?.accessToken,
    staleTime: 1000 * 60 * 60, // 1 hour
    gcTime: 1000 * 60 * 60 * 24, // 24 hours (formerly cacheTime)
  });
}

/**
 * Fetch bot-specific templates from the backend.
 * Used in TemplateGallery component.
 */
export function useTemplates(botId?: string) {
  const { data: session } = useSession();

  return useQuery<TemplatesResponse>({
    queryKey: ['templates', botId],
    queryFn: async () => {
      if (!session?.accessToken) {
        throw new Error('Not authenticated');
      }
      const endpoint = botId
        ? `/v1/bots/${botId}/templates`
        : '/v1/templates';
      return api.get<TemplatesResponse>(
        endpoint,
        session.accessToken as string
      );
    },
    enabled: !!session?.accessToken,
    staleTime: 1000 * 60 * 60, // 1 hour
    gcTime: 1000 * 60 * 60 * 24,
  });
}

/**
 * Fetch all available integrations from the backend.
 * Used in IntegrationMarketplace component.
 */
export function useIntegrations() {
  const { data: session } = useSession();

  return useQuery<IntegrationsResponse>({
    queryKey: ['integrations'],
    queryFn: async () => {
      if (!session?.accessToken) {
        throw new Error('Not authenticated');
      }
      return api.get<IntegrationsResponse>(
        '/v1/integrations/catalog',
        session.accessToken as string
      );
    },
    enabled: !!session?.accessToken,
    staleTime: 1000 * 60 * 60, // 1 hour
    gcTime: 1000 * 60 * 60 * 24,
  });
}

/**
 * Helper hook to invalidate and refetch templates after creating a new one.
 */
export function useInvalidateTemplates() {
  const queryClient = useQueryClient();

  return (botId?: string) => {
    queryClient.invalidateQueries({
      queryKey: botId ? ['templates', botId] : ['templates'],
    });
  };
}

/**
 * Helper hook to invalidate integrations cache.
 */
export function useInvalidateIntegrations() {
  const queryClient = useQueryClient();

  return () => {
    queryClient.invalidateQueries({
      queryKey: ['integrations'],
    });
  };
}

/**
 * Helper hook to invalidate node catalog cache.
 */
export function useInvalidateNodeCatalog() {
  const queryClient = useQueryClient();

  return () => {
    queryClient.invalidateQueries({
      queryKey: ['node-catalog'],
    });
  };
}
