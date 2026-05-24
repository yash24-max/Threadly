import { useQuery, UseQueryResult } from "@tanstack/react-query";
import { api } from "@/lib/api-client";

/**
 * Node catalog entry from backend
 */
export interface NodeCatalogEntry {
  type: string;
  label: string;
  description: string;
  icon: string;
  category: "Messaging" | "Logic" | "AI" | "Integration" | "Flow Control";
  color: string;
  defaultData: Record<string, unknown>;
  inputs?: number;
  outputs?: number;
}

/**
 * Template definition
 */
export interface Template {
  id: string;
  name: string;
  description: string;
  category:
    | "Support"
    | "LeadGen"
    | "Ecommerce"
    | "Healthcare"
    | "RealEstate"
    | "Education"
    | "HR";
  nodeCount: number;
  avatar?: string;
  definition?: Record<string, unknown>;
  isCustom?: boolean;
  createdAt?: string;
}

/**
 * Integration definition
 */
export interface Integration {
  id: string;
  name: string;
  description: string;
  category: string;
  icon: string;
  color: string;
  isConnected: boolean;
  nodeType: string;
  authType: "oauth" | "api_key" | "none";
  scopes?: string[];
  connectUrl?: string;
  docUrl?: string;
}

/**
 * Fetch all available node types for the flow builder
 * Cached for 24h in backend and 5m in client
 */
export const useNodeCatalog = (): UseQueryResult<NodeCatalogEntry[], Error> => {
  return useQuery({
    queryKey: ["catalog", "node-types"],
    queryFn: async () => {
      const response = await api.get("/v1/catalogs/node-types");
      return response.data;
    },
    staleTime: 1000 * 60 * 5, // 5 minutes
    gcTime: 1000 * 60 * 60, // 1 hour (formerly cacheTime)
    retry: 3,
    retryDelay: (attempt) => Math.min(1000 * 2 ** attempt, 30000),
  });
};

/**
 * Fetch all available templates for this org
 * Includes both built-in and custom templates
 * Cached for 5 minutes
 */
export const useTemplates = (): UseQueryResult<Template[], Error> => {
  return useQuery({
    queryKey: ["catalog", "templates"],
    queryFn: async () => {
      const response = await api.get("/v1/catalogs/templates");
      return response.data;
    },
    staleTime: 1000 * 60 * 5, // 5 minutes
    gcTime: 1000 * 60 * 60, // 1 hour
    retry: 3,
    retryDelay: (attempt) => Math.min(1000 * 2 ** attempt, 30000),
  });
};

/**
 * Fetch templates filtered by category
 */
export const useTemplatesByCategory = (
  category: string
): UseQueryResult<Template[], Error> => {
  const { data: templates, ...rest } = useTemplates();

  return {
    ...rest,
    data: templates?.filter((t) => t.category === category),
  };
};

/**
 * Fetch all available integrations for this org
 * Shows which ones are already connected
 * Cached for 5 minutes
 */
export const useIntegrations = (): UseQueryResult<Integration[], Error> => {
  return useQuery({
    queryKey: ["catalog", "integrations"],
    queryFn: async () => {
      const response = await api.get("/v1/catalogs/integrations");
      return response.data;
    },
    staleTime: 1000 * 60 * 5, // 5 minutes
    gcTime: 1000 * 60 * 60, // 1 hour
    retry: 3,
    retryDelay: (attempt) => Math.min(1000 * 2 ** attempt, 30000),
  });
};

/**
 * Search integrations by name, category, or description
 */
export const useSearchIntegrations = (
  query: string
): UseQueryResult<Integration[], Error> => {
  return useQuery({
    queryKey: ["catalog", "integrations", "search", query],
    queryFn: async () => {
      const response = await api.post("/v1/catalogs/integrations/search", {
        query,
      });
      return response.data;
    },
    enabled: query.length > 0,
    staleTime: 1000 * 60 * 5,
    gcTime: 1000 * 60 * 60,
    retry: 3,
    retryDelay: (attempt) => Math.min(1000 * 2 ** attempt, 30000),
  });
};

/**
 * Get node catalog organized by category
 */
export const useNodesByCategory = (): UseQueryResult<
  Record<string, NodeCatalogEntry[]>,
  Error
> => {
  const { data: nodes, ...rest } = useNodeCatalog();

  const organized = nodes?.reduce(
    (acc, node) => {
      if (!acc[node.category]) {
        acc[node.category] = [];
      }
      acc[node.category].push(node);
      return acc;
    },
    {} as Record<string, NodeCatalogEntry[]>
  );

  return {
    ...rest,
    data: organized,
  };
};
