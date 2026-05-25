# Threadly Quick Start - Build Your First Bot in 5 Minutes

**Difficulty:** Beginner  
**Time:** 5 minutes  
**Result:** A working customer support bot on your website

---

## Step 1: Sign Up (30 seconds)

1. Go to https://threadly.dev
2. Click "Get Started"
3. Enter email & password
4. Click "Create Account"
5. Verify your email

---

## Step 2: Create a Bot (30 seconds)

1. Click "Create Bot"
2. Select "Customer Support" template
3. Name it "Support Bot"
4. Click "Create"
5. Wait for bot to initialize (5 seconds)

---

## Step 3: Design the Flow (2 minutes)

Your bot has a default flow:
```
Start → Message → AI Reply → End
```

**Edit the Message node:**
1. Click the "Message" node
2. Change text to: "How can I help you today?"
3. Click outside to save

**Edit the AI Reply node:**
1. Click "AI Reply"
2. Set prompt: "You are a helpful customer support agent. Answer questions about our product. Be friendly and concise."
3. Leave "Use knowledge base" unchecked for now
4. Click outside

**Verify the flow:**
- Green checkmarks on all nodes mean it's valid
- Click "Publish" button (top right)

---

## Step 4: Embed on Your Website (2 minutes)

1. Click "Embed" tab
2. Copy the code snippet:
```html
<script 
  src="https://cdn.threadly.dev/widget.js"
  data-bot="YOUR-BOT-ID-HERE">
</script>
```

3. Go to your website HTML
4. Paste the snippet before `</body>` tag
5. Replace `YOUR-BOT-ID-HERE` with your bot ID (from Embed tab)
6. Save & reload your website

---

## Step 5: Test It Out (30 seconds)

1. Go to your website
2. Click the chat button (bottom right)
3. Type: "How do I reset my password?"
4. AI should respond intelligently

---

## What Just Happened?

Your bot is now:
- Listening for visitor messages on your website
- Sending them through Threadly's AI service
- Returning intelligent responses
- All in < 200ms (p95)

---

## Next Steps

### Add a Knowledge Base (5 min)

To make responses more accurate:

1. Go to "Knowledge Base" tab
2. Upload a PDF with your documentation
3. Wait for ingestion (progress bar)
4. In AI Reply node, toggle "Use knowledge base" ON
5. Publish & test again

### Customize the Widget (2 min)

In Embed tab:

```html
<script 
  src="https://cdn.threadly.dev/widget.js"
  data-bot="YOUR-BOT-ID"
  data-color="#4F46E5"
  data-position="bottom-right"
  data-avatar="https://yoursite.com/avatar.png">
</script>
```

Options:
- `data-color`: Any hex color
- `data-position`: bottom-left, bottom-right, top-left, top-right
- `data-avatar`: URL to image

### Add More Nodes (10 min)

Make conversation more interactive:

1. In builder, drag "Question" node
2. Configure: "What's your email?" (required)
3. Connect to AI Reply
4. Test: Bot now collects email before responding

---

## Common Issues

### Widget not showing?

- [ ] Bot ID is correct (copy from Embed tab)
- [ ] Script tag is before `</body>`
- [ ] No JavaScript errors (check browser console)
- [ ] Site allows external scripts (check CORS)

### AI responses are generic?

- [ ] Improve your prompt in AI Reply node
- [ ] Add a knowledge base with your docs
- [ ] Include examples in the prompt

### Messages not saving?

- [ ] Check internet connection
- [ ] Reload page
- [ ] Check browser console for errors
- [ ] Contact support

---

## Congratulations!

You've successfully:
- Created a chatbot without coding
- Deployed it to your website
- Tested end-to-end communication

You're ready to:
- Add more complex flows
- Upload knowledge documents
- Customize appearance
- Monitor conversations
- Scale to production

---

## Learn More

- [Visual Flow Editor](../architecture/07-flow-spec.md)
- [Knowledge Base Guide](../architecture/09-widget-embed-guide.md)
- [Widget Customization](../architecture/18-widget-guide.md)
- [Troubleshooting](TROUBLESHOOTING.md)

---

## Support

- Email: support@threadly.dev
- Chat: Click widget on threadly.dev
- Docs: https://docs.threadly.dev
