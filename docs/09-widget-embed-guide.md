# Widget Embed Guide

## Quickstart

Add this one line before `</body>` on your website:

```html
<script
  src="https://cdn.threadly.dev/widget.js"
  data-bot="YOUR_BOT_ID"
  async
></script>
```

That's it. The widget appears as a floating chat button in the bottom-right corner.

## Customization

All options are set via `data-*` attributes:

```html
<script
  src="https://cdn.threadly.dev/widget.js"
  data-bot="YOUR_BOT_ID"
  data-color="#4F46E5"
  data-position="bottom-right"
  data-greeting="Hi! How can I help?"
  data-avatar="https://yoursite.com/avatar.png"
  data-launcher-text="Chat with us"
  async
></script>
```

| Attribute | Default | Options |
|---|---|---|
| `data-color` | `#4F46E5` | Any hex color |
| `data-position` | `bottom-right` | `bottom-right`, `bottom-left` |
| `data-greeting` | From bot config | Override greeting message |
| `data-avatar` | From bot config | URL to avatar image |
| `data-launcher-text` | `Chat` | Text next to launcher button |
| `data-dark` | `auto` | `true`, `false`, `auto` (follows OS) |

## JavaScript API

```javascript
// Open the widget
window.Threadly.open();

// Close the widget
window.Threadly.close();

// Set visitor identity (for known users)
window.Threadly.identify({
  id: 'user-123',
  name: 'Priya Sharma',
  email: 'priya@example.com'
});

// Listen for events
window.Threadly.on('open', () => console.log('Widget opened'));
window.Threadly.on('message', (msg) => console.log('New message', msg));
window.Threadly.on('handoff', () => console.log('Handed off to human'));
```

## CORS
The widget makes requests to `api.threadly.dev`. You must allow-list your domain in the bot settings for security.

## Bundle size
- Loader script: ~4 KB gzipped
- Main widget bundle: ~30 KB gzipped (loaded on first interaction only)

## Browser support
Chrome 90+, Firefox 90+, Safari 14+, Edge 90+, mobile browsers.
