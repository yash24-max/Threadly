# Multi-stage Dockerfile for Threadly Widget (Preact + Vite)
# Optimized for minimal bundle size and fast CDN delivery

# Stage 1: Dependencies
FROM node:20-alpine AS dependencies

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci --omit=dev && \
    npm cache clean --force

# Stage 2: Build
FROM node:20-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Build with optimizations
RUN npm run build

# Verify bundle size
RUN echo "Bundle size check:" && \
    du -sh dist/ && \
    if [ -f dist/widget.js ]; then \
      SIZE=$(du -sk dist/widget.js | cut -f1); \
      echo "widget.js: ${SIZE}KB"; \
      if [ "$SIZE" -gt 100 ]; then \
        echo "WARNING: Bundle size exceeds 100KB uncompressed (target: 35KB gzipped)"; \
      fi; \
    fi

# Stage 3: Runtime
FROM node:20-alpine AS runtime

WORKDIR /app

# Create non-root user
RUN addgroup -g 1001 widget && \
    adduser -D -u 1001 -G widget widget

# Copy built widget from builder
COPY --from=builder --chown=widget:widget /app/dist ./dist

# Copy package.json for reference
COPY --chown=widget:widget package.json .

# Optional: Include a simple HTTP server for local development
RUN npm install -g http-server

USER widget

EXPOSE 8080

# Serve the built widget
CMD ["http-server", "dist", "-p", "8080", "-c-1"]
