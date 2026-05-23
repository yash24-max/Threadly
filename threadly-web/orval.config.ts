import { defineConfig } from "orval";

export default defineConfig({
  threadly: {
    input: {
      target: process.env.NEXT_PUBLIC_API_URL
        ? `${process.env.NEXT_PUBLIC_API_URL}/v3/api-docs`
        : "http://localhost:8080/v3/api-docs",
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
