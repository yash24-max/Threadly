import { defineConfig } from "orval";

/**
 * Orval OpenAPI code generation configuration.
 * Fetches the unified OpenAPI schema from the Nginx gateway at localhost:8080.
 * The gateway routes all API requests to the appropriate microservice:
 * - /auth/* → identity-service:3001
 * - /bots/* → workspace-service:3002
 * - /flows/* → flow-service:3003
 * - /sessions/* → runtime-service:3004
 * - /conversations/* → conversation-service:3005
 * - /kb/* → knowledge-service:3006
 * - /dashboard/* → analytics-service:3007
 * - /billing/* → billing-service:3008
 * - /integrations/* → integration-service:3009
 *
 * Regenerate hooks after Nginx gateway is running: npm run codegen
 */
export default defineConfig({
  threadly: {
    input: {
      target: process.env.NEXT_PUBLIC_API_URL
        ? `${process.env.NEXT_PUBLIC_API_URL}/openapi.json`
        : "http://localhost:8080/openapi.json",
    },
    output: {
      mode: "tags-split",
      target: "./lib/generated/api.ts",
      schemas: "./lib/generated/model",
      client: "react-query",
      httpClient: "fetch",
      override: {
        mutator: {
          path: "./lib/api-mutator.ts",
          name: "customInstance",
        },
        query: {
          useQuery: true,
          useMutation: true,
        },
      },
    },
  },
});
