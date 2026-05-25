# Threadly Troubleshooting Guide

**Last Updated:** May 25, 2026

---

## Widget Issues

### Widget not showing on my website

**Symptoms:** Chat button doesn't appear on your website

**Cause:** Usually script placement or bot ID issue

**Solutions:**
1. Verify bot ID is correct (copy from Embed tab)
2. Check script tag is before `</body>` (not in `<head>`)
3. Clear browser cache (Ctrl+Shift+Delete)
4. Check browser console (F12) for errors
5. Verify your domain is whitelisted in bot settings

**Test:**
```html
<!-- Correct placement (before closing body tag) -->
<body>
  ... your content ...
  <script src="https://cdn.threadly.dev/widget.js" 
    data-bot="bot-id-here"></script>
</body>
```

---

### Widget loads but chat doesn't work

**Symptoms:** Widget appears but messages don't send

**Cause:** Server connectivity or permission issue

**Solutions:**
1. Check internet connection
2. Open browser console (F12) for JavaScript errors
3. Verify bot is published (green "Published" badge in builder)
4. Check bot API key is configured
5. Wait 30 seconds for changes to propagate

**Debug:**
```javascript
// In browser console
fetch('https://api.threadly.dev/health')
  .then(r => r.json())
  .then(console.log)
  .catch(console.error)
```

---

### Widget is very slow

**Symptoms:** Chat takes >1 second to load, messages take >3 seconds to reply

**Cause:** Network latency or LLM provider slowness

**Solutions:**
1. Check your internet connection (run speedtest.net)
2. Measure: is it widget load or AI response?
   - Widget load: Check Network tab in DevTools (should be <200ms)
   - AI response: See step 3
3. If AI is slow, you might be hitting LLM rate limits:
   - Check analytics dashboard for error rate
   - Try switching LLM provider in AI Reply node
   - Reduce "temperature" setting (makes responses faster)

---

### Widget customization not working

**Symptoms:** Colors, avatar, or position changes don't apply

**Cause:** Script not reloaded with new parameters

**Solutions:**
1. Hard-refresh page (Ctrl+Shift+R or Cmd+Shift+R)
2. Clear browser cache
3. Wait 60 seconds (CDN cache)
4. Verify syntax is correct:

```html
<!-- Correct -->
<script
  src="https://cdn.threadly.dev/widget.js"
  data-bot="bot-id"
  data-color="#4F46E5"
  data-position="bottom-right">
</script>
```

---

## Bot & Flow Issues

### AI responses are generic or wrong

**Symptoms:** Bot ignores context, gives irrelevant answers

**Cause:** Poor prompt engineering or missing knowledge base

**Solutions:**
1. **Improve prompt:**
   - In AI Reply node, edit prompt
   - Add examples: "Q: How to reset? A: Go to Settings > Reset password"
   - Be specific about bot personality: "You are a friendly support agent"

2. **Add knowledge base:**
   - Upload your product docs (PDF)
   - Toggle "Use knowledge base" in AI Reply
   - Test with specific questions from your docs

3. **Test prompt:** Use "Test" button in builder to dry-run

**Better Prompt Example:**
```
You are a helpful customer support agent for ACME Widget Company.
You help customers troubleshoot issues and answer product questions.

Guidelines:
- Be friendly and concise (max 100 words)
- If unsure, ask for clarification
- Escalate to human if issue is complex
- Use provided knowledge base for facts

Current conversation: {conversation_history}
Knowledge base passages: {kb_passages}

User question: {user_input}
Your response:
```

---

### KB search returns wrong documents

**Symptoms:** Knowledge base documents aren't relevant to queries

**Cause:** Poor document quality, wrong chunking, or vague query

**Solutions:**
1. **Check documents:**
   - Go to Knowledge Base tab
   - Review chunk previews
   - Ensure documents are text-extractable (not scanned images)
   - Test with exact phrases from docs

2. **Re-upload if needed:**
   - Delete document
   - Re-upload with better formatting
   - Wait for re-ingestion (progress bar)

3. **Improve queries:**
   - Instead of "help", say "how to reset password"
   - Use exact product names
   - Include context

4. **Monitor:**
   - In analytics, check "KB search relevance"
   - If <80%, improve documents or prompts

---

### Conversations not saving

**Symptoms:** Messages disappear, history empty, can't find old conversations

**Cause:** Database connection or permission issue

**Solutions:**
1. Check backend health: https://api.threadly.dev/health
2. Verify conversation service is running: https://api.threadly.dev/conversations/health
3. Check for database errors in logs
4. Try refreshing the page
5. Contact support if persists

---

### Real-time updates not working

**Symptoms:** Conversations don't update in real-time, need to refresh to see new messages

**Cause:** WebSocket connection dropped

**Solutions:**
1. Check your firewall (WebSocket might be blocked)
2. Disable browser extensions (especially ad blockers)
3. Check browser console for errors
4. Try incognito mode (rules out extensions)
5. Try different network (WiFi vs mobile data)

**Test WebSocket:**
```javascript
// In browser console
const ws = new WebSocket('wss://api.threadly.dev/realtime');
ws.onopen = () => console.log('Connected!');
ws.onerror = (e) => console.log('Error:', e);
ws.onmessage = (e) => console.log('Message:', e.data);
```

---

## Authentication Issues

### Can't sign up or login

**Symptoms:** "Invalid email" or "Password too weak" error

**Solutions:**
1. **Sign up:**
   - Email must be valid format (name@domain.com)
   - Password must be 8+ characters, 1 uppercase, 1 number, 1 symbol
   - Check email inbox for verification link

2. **Login:**
   - Verify caps lock is off
   - Try password reset (forgot password link)
   - Clear browser cookies

3. **Verification email not arriving:**
   - Check spam folder
   - Verify email address is correct
   - Wait 5 minutes and refresh
   - Resend verification email

---

### Lost access to account

**Symptoms:** Can't login, forgot password, account locked

**Solutions:**
1. Click "Forgot Password" on login page
2. Enter email address
3. Check email for reset link
4. Follow link and set new password
5. Login with new password

**If email doesn't arrive:**
1. Check spam folder
2. Wait 5 minutes
3. Click "Resend" on password reset page
4. Contact support if still broken

---

## Performance Issues

### API is slow (>500ms)

**Symptoms:** Requests taking >500ms, API calls timing out

**Cause:** High server load, database slow, or network issue

**Solutions:**
1. Check status page: https://status.threadly.dev
2. Measure latency: open DevTools Network tab, check response time
3. If consistently slow:
   - Check if request is large (>10MB)
   - Try at different time of day
   - Contact support with request logs

---

### Database errors or timeouts

**Symptoms:** "Connection timeout", "Deadlock", "Query timeout"

**Solutions:**
1. Retry request (usually temporary)
2. Wait 30 seconds and try again
3. Check status page
4. Reduce request size (e.g., fewer conversations to fetch)
5. Contact support with error message

---

## Billing & Account

### Why was I charged for conversations I didn't use?

**Cause:** Conversations include test/internal usage

**Solutions:**
1. Go to Billing > Usage
2. See breakdown by day
3. Look for unusual spikes
4. Test conversations count (toggle "exclude test" if available)
5. Contact support for credit if error

---

### How do I change my plan?

**Steps:**
1. Go to Billing
2. See current plan
3. Click "Upgrade" or "Downgrade"
4. Select new plan
5. Confirm

Changes take effect immediately. Prorated based on usage.

---

## Still Having Issues?

### Before Contacting Support

1. Clear browser cache
2. Try incognito mode
3. Try different browser
4. Check status page
5. Review this guide

### Contact Support

- Email: support@threadly.dev
- Response time: <2 hours
- In-app chat: Click widget on threadly.dev

**Include:**
- Bot ID
- Your email address
- Description of issue
- Steps to reproduce
- Screenshots if helpful
- Timestamp when issue occurred
- Browser & OS version

---

## Common Solutions Summary

| Issue | Solution |
|-------|----------|
| Widget not showing | Check bot ID, script placement, cache |
| Slow responses | Check network, try different LLM, improve prompt |
| KB search wrong | Re-upload docs, use exact phrases |
| Messages not saving | Check backend health, refresh page |
| Real-time not working | Check WebSocket, disable extensions, try incognito |
| Can't login | Check email, verify password, try password reset |
| API slow | Check status page, measure latency, contact support |

---

## Escalation Path

1. **Self-help:** Check this guide + docs
2. **Community:** Post in Threadly community Slack
3. **Email support:** support@threadly.dev
4. **Priority support:** Available with Business plan+
5. **Emergency:** VIP hotline (Enterprise only)

---

**Last Updated:** May 25, 2026  
**Need help?** support@threadly.dev
