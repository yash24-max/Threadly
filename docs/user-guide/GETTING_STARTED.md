# Threadly — Getting Started Guide

> Your first 30 minutes with Threadly: From signup to live chatbot.

---

## What is Threadly?

Threadly is an **AI chatbot builder** that lets you create intelligent conversational experiences for your website in minutes — no coding required.

### Use Cases

- **Customer Support:** Auto-answer FAQs; hand off complex issues to humans
- **Lead Generation:** Capture visitor emails; qualify leads with screening questions
- **Sales Acceleration:** Guide prospects through your product; collect demo requests
- **Onboarding:** Interactive walkthroughs; proactive help for new customers

---

## Step 1: Sign Up (2 minutes)

### Create Your Account

1. Visit [app.threadly.io](https://app.threadly.io)
2. Click **"Sign Up"**
3. Enter your email and password
4. Click **"Create Account"**
5. Check your email for verification link (if required)

You're now logged in! You'll see your **Organization Dashboard**.

---

## Step 2: Create Your First Bot (5 minutes)

### Name Your Bot

1. Click **"+ New Bot"** (or skip the onboarding if you see it)
2. Enter a name (e.g., "Website Support Bot")
3. Select an optional category (Support, Sales, Onboarding, etc.)
4. Click **"Create Bot"**

### Customize Appearance

1. Click on your bot's card → **"Settings"**
2. Under **"Appearance":**
   - Add a profile picture (or use default avatar)
   - Set bot name as visitors will see it (e.g., "Sarah - Support Team")
   - Customize colors to match your brand
3. Click **"Save"**

---

## Step 3: Build Your First Flow (10 minutes)

A **Flow** is the conversation logic. It defines what your bot says, when it asks questions, and how it responds.

### Enter the Flow Builder

1. From your bot's dashboard, click **"Edit Flow"**
2. You'll see a blank canvas with a **"Start"** node

### Add a Welcome Message

1. Click **"+ Add Node"** (or drag from the left panel)
2. Select **"Message"** node
3. Edit the message:
   ```
   Hi there! 👋 I'm here to help. What can I do for you?
   ```
4. Click outside to save

### Add a Question Node

1. Add another node after the message: **"Question"**
2. Set the question text:
   ```
   What's your issue about?
   ```
3. Add options (buttons):
   - "Pricing"
   - "Technical Issue"
   - "General Question"
4. Connect each option to a different response (we'll add those next)

### Add Response Messages

1. For each option, add a **"Message"** node with a relevant response:
   - "Pricing" → "Our plans start at $29/month..."
   - "Technical Issue" → "I'll connect you with our support team..."
   - "General Question" → "Happy to help! Tell me more..."

### Connect Nodes

1. Hover between nodes and drag the connector
2. Connect your question to its response branches
3. Each response flows to an **"End"** node (or to next question)

### Preview Your Flow

1. Click **"Preview"** (top-right)
2. Chat with your bot as a visitor would
3. Click **"Done"** to exit preview

---

## Step 4: Add Knowledge Base (Optional, 5 minutes)

Your **Knowledge Base** teaches your bot to answer questions using your own documents.

### Upload Documents

1. In your bot's dashboard, click **"Knowledge Base"**
2. Click **"+ Upload Document"**
3. Choose a PDF, Word, or text file (e.g., FAQ, product manual)
4. Select **"Upload"**

Threadly will:
- Extract text from your document
- Break it into chunks
- Create searchable embeddings
- Make it available to your bot's AI

### Use Knowledge in Your Flow

1. Go back to your flow builder
2. When you add an **"AI Reply"** node, it automatically uses your knowledge base
3. The AI will cite sources: `[1]`, `[2]`, etc.

---

## Step 5: Publish & Embed (5 minutes)

### Publish Your Flow

1. In the flow builder, click **"Publish"** (top-right)
2. Confirm the version (it will say "v1")
3. Your live conversations will use this published version

### Get Your Embed Code

1. Go to your bot's dashboard
2. Click **"Settings"** → **"Embed"**
3. Copy the embed code:
   ```html
   <script src="https://cdn.threadly.io/widget.js"></script>
   <script>
     ThreadlyWidget.init({
       botId: "your-bot-id",
       position: "bottom-right"
     });
   </script>
   ```

### Add to Your Website

1. Open your website's HTML (or admin panel if using WordPress, Webflow, etc.)
2. Paste the code into the `<body>` section (before closing `</body>` tag)
3. Save and reload your site
4. You should see the chat widget in the bottom-right corner!

### Test the Live Bot

1. Reload your website
2. Click the chat widget
3. Talk to your bot
4. Verify it responds as expected

---

## Step 6: Monitor Conversations (Ongoing)

### View Your Conversations

1. In Threadly, click **"Inbox"**
2. You'll see all conversations with your bot
3. Click any conversation to read the full transcript

### Take Over from Your Bot

If a visitor has a complex issue:

1. Click the conversation
2. Click **"Take Over"** (you're now chatting as a human)
3. Type your response
4. When done, click **"Resume AI"** to hand back to the bot

### Track Performance

1. Click **"Analytics"**
2. You'll see:
   - Total conversations
   - AI cost
   - Handoff rate
   - Completion funnel

---

## Next Steps: Advanced Features

Once you're comfortable, explore:

### AI Replies

Instead of static messages, have your bot **generate responses using AI**:

1. Add an **"AI Reply"** node
2. Set the context or instructions
3. Your knowledge base will automatically be used to ground responses

### Integrations

Connect your bot to external tools:
- **Slack:** Get notified of new conversations
- **HubSpot:** Auto-create leads
- **Google Sheets:** Log conversations
- **Webhooks:** Send data to your backend

### Lead Capture

Collect visitor information:

1. Add a **"Collect Input"** node
2. Ask for email, phone, or custom fields
3. Leads appear in your CRM

### Flow Templates

Don't build from scratch:

1. Click **"Templates"** in your bot's dashboard
2. Choose a pre-built flow (Support, Sales, Onboarding, etc.)
3. Customize it for your use case

---

## Common Patterns

### Pattern 1: FAQ Bot

```
Start → Welcome Message
       → Question: "What's your question?"
         ├─ "How do I reset my password?" → AI Reply (searches knowledge base)
         ├─ "Where are you located?" → Static Message
         └─ "Something else" → Handoff to Human
```

### Pattern 2: Lead Capture

```
Start → Welcome Message
       → Collect Input (email, company, needs)
       → Message: "Thanks! We'll be in touch"
       → Integrate with HubSpot to create lead
       → End
```

### Pattern 3: Product Demo Qualifier

```
Start → Message: "Interested in a demo?"
       → Question: "How many team members?" (buttons: 1-5, 5-20, 20+)
       → Message: "Great! Let's find the right plan"
       → Handoff to sales agent
```

---

## Troubleshooting

### My bot isn't appearing on my website

- Check that you pasted the embed code in your website's `<body>`
- Wait 5 minutes for the code to be delivered to your CDN
- Clear your browser cache and refresh
- Check browser console for errors (F12 → Console tab)

### The bot isn't using my knowledge base

- Make sure you uploaded documents to the Knowledge Base
- Your document must have searchable text (PDFs with images won't work)
- Wait a few minutes for embeddings to process
- Test with a specific term from your document

### Conversations aren't appearing in my inbox

- Make sure you published your flow (changes to draft don't go live)
- Check that the widget is showing on your website
- Try opening an incognito window and chatting again
- Refresh your inbox page

### How do I update my live bot?

1. Make changes in the flow builder (draft)
2. Test with Preview
3. Click **"Publish"** to make it live
4. All new conversations will use the new version immediately

---

## Need Help?

- **Documentation:** Read detailed guides at [docs.threadly.io](https://docs.threadly.io)
- **Tutorials:** Watch video guides on YouTube (coming soon)
- **Community:** Ask questions on our [community forum](https://community.threadly.io)
- **Support:** Email support@threadly.io for urgent issues

---

## What's Next?

Congratulations! Your bot is live. Here are some ideas to grow:

1. **Analyze Performance:** Check Analytics to see which questions visitors ask most
2. **Improve Knowledge Base:** Add documents for frequently asked questions
3. **Add Integrations:** Connect to tools your team uses (Slack, HubSpot, etc.)
4. **Customize Design:** Match your bot's appearance to your brand
5. **Set Up CRM:** Use lead capture to build your contact database

**Happy building!** 🚀
