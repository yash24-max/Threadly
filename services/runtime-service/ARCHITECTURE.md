# Runtime Service Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                     External Systems                              │
├──────────────┬──────────────┬──────────────┬──────────────────────┤
│  Flow Store  │  Bot Manager │ AI Services  │  External APIs       │
└──────────────┴──────────────┴──────────────┴──────────────────────┘
                              │
                              │ HTTP
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Runtime Service                                │
├─────────────────────────────────────────────────────────────────┤
│                                                                    │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │              REST Controllers (Layer 1)                     │  │
│  │  ├─ SessionController                                      │  │
│  │  └─ ExecutionController                                    │  │
│  └────────────────────────────────────────────────────────────┘  │
│                           │                                        │
│                           ▼                                        │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │              Service Layer (Layer 2)                       │  │
│  │  ├─ RuntimeExecutor (Main Orchestrator)                   │  │
│  │  ├─ SessionService                                        │  │
│  │  ├─ SessionVariableManager                                │  │
│  │  ├─ FlowInterpreter                                       │  │
│  │  ├─ ConditionEvaluator                                    │  │
│  │  └─ ExecutionTracker                                      │  │
│  └────────────────────────────────────────────────────────────┘  │
│                           │                                        │
│                           ▼                                        │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │          Executor Framework (Layer 3)                      │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │ NodeExecutorFactory                                 │  │  │
│  │  │ (Registry & Factory Pattern)                        │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  │                      │                                      │  │
│  │  ┌───────────────────┼───────────────────┐               │  │
│  │  ▼                   ▼                   ▼               │  │
│  │  NodeExecutor Base   ExecutionContext   ExecutionResult │  │
│  │                                                            │  │
│  │  Concrete Executors:                                     │  │
│  │  ├─ MessageNodeExecutor                                 │  │
│  │  ├─ QuestionNodeExecutor                                │  │
│  │  ├─ ConditionNodeExecutor                               │  │
│  │  ├─ SwitchNodeExecutor                                  │  │
│  │  ├─ SetVariableNodeExecutor                             │  │
│  │  ├─ DelayNodeExecutor                                   │  │
│  │  ├─ ApiCallNodeExecutor                                 │  │
│  │  ├─ AiReplyNodeExecutor                                 │  │
│  │  ├─ ClassifyIntentNodeExecutor                          │  │
│  │  ├─ HandoffNodeExecutor                                 │  │
│  │  ├─ LoopNodeExecutor                                    │  │
│  │  ├─ SubflowNodeExecutor                                 │  │
│  │  ├─ EndNodeExecutor                                     │  │
│  │  └─ (extensible for new node types)                     │  │
│  └────────────────────────────────────────────────────────────┘  │
│                           │                                        │
│                           ▼                                        │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │          Repository Layer (Layer 4)                       │  │
│  │  ├─ SessionRepository                                    │  │
│  │  ├─ SessionVariableRepository                            │  │
│  │  ├─ ExecutionStateRepository                             │  │
│  │  ├─ ExecutionLogRepository                               │  │
│  │  ├─ VisitorProfileRepository                             │  │
│  │  └─ ConversationMemoryRepository                         │  │
│  └────────────────────────────────────────────────────────────┘  │
│                           │                                        │
│                           ▼                                        │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │      Persistence Layer (Layer 5 - Database)              │  │
│  │                                                            │  │
│  │  Tables:                                                  │  │
│  │  ├─ sessions                                              │  │
│  │  ├─ session_variables                                     │  │
│  │  ├─ execution_states                                      │  │
│  │  ├─ execution_logs                                        │  │
│  │  ├─ visitor_profiles                                      │  │
│  │  └─ conversation_memories                                 │  │
│  └────────────────────────────────────────────────────────────┘  │
│                                                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┼─────────────┐
                ▼             ▼             ▼
          PostgreSQL      Kafka        Consul
         (Storage)      (Events)   (Discovery)
```

## Data Flow: Session Execution

```
User Request
    │
    ▼
┌─────────────────────────────┐
│  SessionController          │
│  POST /sessions             │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  SessionService             │
│  - createSession()          │
│  - saveVisitorProfile()     │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  RuntimeExecutor            │
│  - executeFlow()            │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  FlowInterpreter            │
│  - getEntryNode()           │
│  - validateFlow()           │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  NodeExecutorFactory        │
│  - getExecutor()            │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  NodeExecutor (Specific)    │
│  - execute()                │
│  - validate()               │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  ExecutionResult            │
│  - nextNodeId               │
│  - variables                │
│  - messages                 │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  SessionVariableManager     │
│  - setVariables()           │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  ExecutionTracker           │
│  - logExecution()           │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  Repositories               │
│  - save()                   │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  PostgreSQL Database        │
│  - Persist All Data         │
└─────────────────────────────┘
    │
    ▼
┌─────────────────────────────┐
│  Kafka Events               │
│  - SessionCreatedEvent      │
│  - NodeExecutedEvent        │
│  - SessionEndedEvent        │
└─────────────────────────────┘
```

## Session State Transitions

```
                    ┌─────────────┐
                    │   CREATED   │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
        ┌──────────►│   ACTIVE    │◄──────────┐
        │           └──────┬──────┘           │
        │                  │                  │
        │                  ▼                  │
        │           ┌─────────────┐           │
        └───────────┤   PAUSED    │───────────┘
                    │(User Input) │
                    └──────┬──────┘
                           │
                           ▼
                    ┌─────────────┐
                    │   ENDED     │
                    └─────────────┘
                           │
                           ▼
                    ┌─────────────┐
                    │  (Cleanup)  │
                    └─────────────┘
```

## Variable Resolution Flow

```
Text with Variables
  "Hello {{user_name}}, your balance is {{balance}}"
          │                        │
          ▼                        ▼
 ┌───────────────┐        ┌───────────────┐
 │ Extract Vars  │        │ Extract Vars  │
 │ "user_name"   │        │ "balance"     │
 └───────┬───────┘        └───────┬───────┘
         │                        │
         ▼                        ▼
  ┌─────────────────────────────────────────┐
  │  SessionVariableManager                 │
  │  - getAllVariables()                    │
  │  - resolveVariables()                   │
  └─────────────────────────────────────────┘
         │                        │
         ▼                        ▼
 ┌───────────────┐        ┌───────────────┐
 │ "John"        │        │ "500.00"      │
 └───────┬───────┘        └───────┬───────┘
         │                        │
         └────────────┬───────────┘
                      ▼
        "Hello John, your balance is 500.00"
```

## Flow Graph Traversal

```
                    ┌─────────────────┐
                    │  START NODE     │
                    │  (MESSAGE)      │
                    └────────┬────────┘
                             │ next
                             ▼
                    ┌─────────────────┐
                    │  QUESTION NODE  │
                    │  (Ask Name)     │
                    └────────┬────────┘
                             │ response → variable
                             ▼
                    ┌─────────────────┐
                    │  CONDITION NODE │
                    │  (Is Valid?)    │
                    └──────┬──────┬───┘
                   true│    │false
                       ▼    ▼
                   ┌──────┐┌──────┐
                   │ API  ││MSG   │
                   │CALL  ││ERROR │
                   └──┬───┘└──┬───┘
                      │      │
                      ▼      ▼
                   ┌──────────────┐
                   │  END NODE    │
                   │  (MESSAGE)   │
                   └──────────────┘
```

## Concurrency Model

```
Request 1          Request 2          Request 3
   │                  │                  │
   ├─ SessionA        ├─ SessionB        ├─ SessionC
   │  Variables       │  Variables       │  Variables
   │                  │                  │
   ▼                  ▼                  ▼
Session Service
   │
   ├─ Thread Pool (Spring)
   │
   ▼
RuntimeExecutor (Stateless)
   │
   ├─ Node Execution (Serial per session)
   │
   ▼
Database (Row-level Locking via JPA)
   │
   ├─ OptimisticLocking via @Version
   │
   ▼
Results Return Independently
```

## Error Handling Strategy

```
Request
   │
   ▼
┌─────────────────┐
│ Node Execution  │
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
 SUCCESS    FAILURE
    │         │
    ▼         ▼
 Continue   ┌────────────────────┐
            │ GlobalExceptionHandler
            │ - Log error
            │ - Return error response
            │ - Update session state
            │ - Optionally: Branch to ERROR node
            └────────────────────┘
```

## Extension Points

```
╔════════════════════════════════════════════════════════════╗
║         How to Add a New Node Type                         ║
╚════════════════════════════════════════════════════════════╝

1. Create Executor Class
   ├─ Extend NodeExecutor
   ├─ Implement execute() method
   ├─ Add @Component annotation
   └─ Define node type (must be unique)

2. Implement Validation
   ├─ Override validate()
   ├─ Check required fields
   └─ Provide meaningful error messages

3. Register Executor
   ├─ NodeExecutorFactory auto-discovers
   ├─ Via Spring component scanning
   └─ No manual registration needed

4. Add Documentation
   ├─ JavaDoc with examples
   ├─ Input/output specifications
   └─ Error handling details

Example: CustomNodeExecutor
```

## Performance Considerations

```
┌─────────────────────────────────────────────┐
│  Performance Optimization Points            │
├─────────────────────────────────────────────┤
│                                              │
│ 1. Database Indexing                        │
│    ├─ session_id on all tables              │
│    ├─ state on sessions                     │
│    └─ created_at for cleanup queries        │
│                                              │
│ 2. Variable Caching                         │
│    ├─ Load once per execution               │
│    ├─ Update in batches                     │
│    └─ Avoid repeated DB queries             │
│                                              │
│ 3. Execution Limits                         │
│    ├─ max-execution-depth: 100              │
│    ├─ node-execution-timeout: 30s           │
│    └─ Prevent runaway flows                 │
│                                              │
│ 4. Connection Pooling                       │
│    ├─ Hikari CP (default)                   │
│    ├─ Pool size: 10-20 connections          │
│    └─ Idle timeout: 10 minutes              │
│                                              │
│ 5. Logging Strategy                         │
│    ├─ MDC for tracing                       │
│    ├─ Async logging                         │
│    └─ Selective detailed logs               │
│                                              │
└─────────────────────────────────────────────┘
```

## Security Boundaries

```
┌────────────────────────────────────────────────┐
│  Security Layers                               │
├────────────────────────────────────────────────┤
│                                                 │
│ Layer 1: API Authentication                    │
│  ├─ Validate caller identity                  │
│  ├─ Check authorization                       │
│  └─ Rate limiting (future)                    │
│                                                 │
│ Layer 2: Input Validation                      │
│  ├─ Validate request DTOs                     │
│  ├─ Sanitize flow definitions                 │
│  └─ Check variable names                      │
│                                                 │
│ Layer 3: Data Access Control                   │
│  ├─ User can only access own sessions         │
│  ├─ Visitor data isolation                    │
│  └─ Bot-level access control                  │
│                                                 │
│ Layer 4: Database Security                     │
│  ├─ Parameterized queries (JPA)               │
│  ├─ SQL injection prevention                  │
│  └─ Least privilege DB user                   │
│                                                 │
└────────────────────────────────────────────────┘
```

## Integration Points

```
External Systems ←→ Runtime Service ←→ Internal Systems

┌──────────────────┐        ┌──────────────────┐
│  Flow Management │        │  Session Service │
│  (Definition)    │───────►│  (Execution)     │
└──────────────────┘        └──────────────────┘
                                     │
                    ┌────────────────┼────────────────┐
                    ▼                ▼                ▼
            ┌──────────────┐  ┌──────────────┐  ┌──────────────┐
            │ AI Service   │  │ External API │  │ Bot Manager  │
            │ (Claude)     │  │ (Webhooks)   │  │ (Metadata)   │
            └──────────────┘  └──────────────┘  └──────────────┘
                    ▲                ▲                ▲
                    └────────────────┼────────────────┘
                                     │
                            ┌────────┴────────┐
                            ▼                 ▼
                        ┌──────────┐    ┌──────────┐
                        │  Kafka   │    │Database  │
                        │ (Events) │    │(State)   │
                        └──────────┘    └──────────┘
```

## Conclusion

This architecture provides:
- **Extensibility**: Add new node types easily
- **Scalability**: Stateless services with database-backed state
- **Reliability**: Error handling and retry mechanisms
- **Observability**: Comprehensive logging and tracing
- **Maintainability**: Clean layered architecture
- **Performance**: Optimized queries and caching strategies
