# Runbooks — Operational Procedures

This directory contains step-by-step operational runbooks for managing Threadly microservices in production.

## Overview

All runbooks follow a consistent structure:
- **Pre-flight checks** — Verify preconditions before starting
- **Step-by-step commands** — Exact procedures with expected outputs
- **Verification & validation** — Confirm success at each stage
- **Rollback procedures** — How to undo if something goes wrong
- **Escalation contacts** — Who to notify if issues persist

---

## Runbooks

### RUNBOOK_MIGRATION.md
**Purpose**: Phase 1-3 migration from monolith to microservices  
**Scope**: Zero-downtime shadow mode → dual-write → cutover  
**Timeline**: 4 weeks (Week 1-4)  
**When to use**: Executing the controlled migration across all services

**Key Sections**:
- Phase 1: Shadow Mode (read-only, parallel execution)
- Phase 2: Dual-Write (parallel monolith + services)
- Phase 3: Cutover (switch to services as primary)
- Validation and data consistency checks

---

### RUNBOOK_SERVICE_RESTART.md
**Purpose**: Emergency restart of individual services  
**Scope**: Single service or batch restart procedures  
**Timeline**: 2-5 minutes per service  
**When to use**: Service is unresponsive, out of memory, or needs configuration reload

**Key Sections**:
- Pre-flight health checks
- Graceful shutdown procedure
- Service restart (with health verification)
- Post-restart validation
- Emergency fast-restart procedure

---

### RUNBOOK_KAFKA_RECOVERY.md
**Purpose**: Handle Kafka consumer lag and recovery scenarios  
**Scope**: Consumer lag detection, DLQ handling, rebalancing  
**Timeline**: 5-30 minutes depending on lag severity  
**When to use**: Kafka consumers are lagging, messages stuck in DLQ, or rebalancing needed

**Key Sections**:
- Monitor consumer lag and lag trends
- Detect and clear stuck messages in DLQ
- Manual rebalancing if needed
- Replay messages from specific offset
- Kafka broker health verification

---

### RUNBOOK_ROLLBACK.md
**Purpose**: Emergency rollback from Phase 3 to Phase 1 (or Phase 2)  
**Scope**: Full microservices → monolith or Phase 3 → Phase 2 rollback  
**Timeline**: < 30 minutes for complete rollback  
**When to use**: Microservices migration has critical issues; need to revert to previous stable state

**Key Sections**:
- Pre-rollback validation
- Switching traffic back to monolith
- Verifying data consistency post-rollback
- Post-rollback monitoring
- When to involve incident commander

---

## Usage Pattern

1. **Identify the issue** — Which service? What's the symptom?
2. **Select appropriate runbook** — Use the table above
3. **Follow pre-flight checks** — Don't skip these
4. **Execute step-by-step** — Follow exact commands, verify at each stage
5. **Validate success** — Use verification commands
6. **Document in incident log** — Record what happened and why
7. **Escalate if blocked** — Use escalation contacts if you get stuck

---

## Quick Reference

| Symptom | Runbook | Quick Action |
|---------|---------|--------------|
| Service down | RUNBOOK_SERVICE_RESTART.md | `make restart-service SERVICE=<name>` |
| Kafka lag > 10k | RUNBOOK_KAFKA_RECOVERY.md | Check consumer group status |
| Migration issues | RUNBOOK_MIGRATION.md | Verify Phase status, check dual-write lag |
| Critical failure | RUNBOOK_ROLLBACK.md | Prepare rollback, notify team lead |

---

## Escalation Path

1. **Service team lead** — First escalation (5 min)
2. **Platform team** — Infrastructure issues (10 min)
3. **Tech lead (@yasva)** — Critical decisions (ASAP)
4. **On-call incident commander** — Post-incident review

---

## Before Running Any Runbook

1. Verify you have access to:
   - Kubernetes cluster (prod)
   - Kafka brokers
   - Database (read/write if needed)
   - Monitoring dashboards

2. Have a colleague ready for pair operations

3. Document your actions in the incident log

4. Have the rollback plan ready before you start

---

## Updates & Maintenance

These runbooks are living documents. Update them when:
- Procedures change due to new infrastructure
- A runbook was used and revealed missing steps
- New failure modes are discovered
- SLA targets change

Always test runbook procedures in staging before using in production.

---

**Last Updated**: 2025-05-24  
**Maintained by**: @yasva  
**Next Review**: 2025-08-24 (quarterly)
