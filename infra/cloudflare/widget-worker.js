/**
 * Cloudflare Worker — Threadly Widget CDN
 * Serves widget bundles from R2 with proper cache headers.
 * Adds CORS headers for cross-origin embedding.
 */
export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const path = url.pathname;

    // CORS preflight
    if (request.method === 'OPTIONS') {
      return new Response(null, {
        headers: {
          'Access-Control-Allow-Origin': '*',
          'Access-Control-Allow-Methods': 'GET, OPTIONS',
          'Access-Control-Max-Age': '86400',
        },
      });
    }

    // Only serve GET
    if (request.method !== 'GET') {
      return new Response('Method Not Allowed', { status: 405 });
    }

    // /widget.js → latest version (no-cache, always fresh)
    if (path === '/widget.js') {
      const obj = await env.WIDGET_BUCKET.get('latest/widget.js');
      if (!obj) return new Response('Not Found', { status: 404 });
      return new Response(obj.body, {
        headers: {
          'Content-Type': 'application/javascript',
          'Cache-Control': 'no-cache, no-store, must-revalidate',
          'Access-Control-Allow-Origin': '*',
          'X-Content-Type-Options': 'nosniff',
        },
      });
    }

    // /widget/v{version}.js → immutable versioned bundle
    const versionedMatch = path.match(/^\/widget\/v([\d.]+)\.js$/);
    if (versionedMatch) {
      const version = versionedMatch[1];
      const obj = await env.WIDGET_BUCKET.get(`v${version}/widget.js`);
      if (!obj) return new Response('Not Found', { status: 404 });

      const cacheKey = new Request(url.toString(), request);
      const cached = await caches.default.match(cacheKey);
      if (cached) return cached;

      const response = new Response(obj.body, {
        headers: {
          'Content-Type': 'application/javascript',
          'Cache-Control': 'public, max-age=31536000, immutable',
          'Access-Control-Allow-Origin': '*',
          'X-Content-Type-Options': 'nosniff',
        },
      });

      ctx.waitUntil(caches.default.put(cacheKey, response.clone()));
      return response;
    }

    return new Response('Not Found', { status: 404 });
  },
};
