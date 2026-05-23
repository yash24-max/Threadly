-- V5: Analytics events index improvements + node_id on messages for funnel analysis

-- Add node_id index on messages for funnel queries
CREATE INDEX IF NOT EXISTS messages_node_id_idx
  ON messages ((metadata->>'nodeId'))
  WHERE metadata ? 'nodeId';

-- Add bot_id to events index for per-bot analytics
CREATE INDEX IF NOT EXISTS events_bot_id_type_idx
  ON events (bot_id, event_type, created_at)
  WHERE bot_id IS NOT NULL;

-- Intent events index
CREATE INDEX IF NOT EXISTS events_intent_idx
  ON events (org_id, bot_id, (metadata->>'intent'))
  WHERE event_type = 'intent.detected' AND metadata ? 'intent';

-- Satisfaction rating index on conversations
CREATE INDEX IF NOT EXISTS conversations_rating_idx
  ON conversations (org_id, bot_id)
  WHERE metadata ? 'rating';
