# 🌳 GIT WORKTREES - COMPLETE GUIDE

**Created:** May 25, 2026
**Project:** Threadly Microservices

---

## ✅ CURRENT STATUS

### Git Status Check
```
✅ Branch: main
✅ Up to date with origin/main
✅ .claude/ folder: PROPERLY GITIGNORED (line 75 in .gitignore)
✅ Worktrees: NOT committed to git
```

### Gitignore Configuration
```
Location: .threadly/.gitignore (Line 74-75)
Pattern: .claude/
Status: ✅ ACTIVE & WORKING

This ignores:
✅ .claude/worktrees/ (all worktrees)
✅ Session data
✅ Memory files
✅ Temporary Claude Code files
```

### Active Worktrees in Project
```
┌─────────────────────────────────────────────────────┐
│ Main Worktree                                       │
│ /Users/yasva/Kapture/Microservice/Project/Threadly │
│ Branch: main                                        │
│ Commit: 8b9592f [main]                              │
└─────────────────────────────────────────────────────┘

Temporary Worktrees (Claude Code):
├─ agent-a0ef3236 (Commit: 8fd688b, Branch: worktree-agent-a0ef3236)
├─ agent-ad05d94b (Commit: 0c89a18, Branch: worktree-agent-ad05d94b)
├─ agent-ad522042 (Commit: 24064f2, Branch: worktree-agent-ad522042)
├─ agent-ae84da91 (Commit: d4970f0, Branch: worktree-agent-ae84da91)
└─ agent-ae8e7baf (Commit: 8966f8e, Branch: worktree-agent-ae8e7baf)
```

---

## 🤔 WHAT ARE GIT WORKTREES?

### Simple Explanation

Git worktrees allow you to have **multiple working directories from the same repository at the same time**.

```
Traditional Git:
┌──────────────────────────┐
│ Single Working Directory │
│ .git/                    │
│ src/                     │
│ pom.xml                  │
│                          │
│ Can work on 1 branch     │
│ Must switch branches     │
│ Can't have 2 branches    │
│ at same time             │
└──────────────────────────┘

With Git Worktrees:
┌──────────────────────────┐ ┌──────────────────────────┐
│ Worktree 1 (main)        │ │ Worktree 2 (feature-a)   │
│ .git -> main repo        │ │ .git -> main repo        │
│ src/                     │ │ src/                     │
│                          │ │                          │
│ Working on main branch   │ │ Working on feature-a     │
└──────────────────────────┘ └──────────────────────────┘

Both at SAME TIME!
```

### Key Concept

One `.git` directory (central repository) + Multiple working directories

```
Central Repository (.git)
    ├─ Worktree 1: /main (branch: main)
    ├─ Worktree 2: /feature-a (branch: feature-a)
    ├─ Worktree 3: /feature-b (branch: feature-b)
    └─ Worktree 4: /hotfix (branch: hotfix)
```

---

## 🎯 USE CASES FOR GIT WORKTREES

### Use Case 1: Parallel Development

**Scenario:** Working on 2 features simultaneously

**Without Worktrees:**
```bash
# Working on feature A
git checkout feature-a
# Make changes
git add .
git commit -m "Feature A work"

# Now need to work on feature B
git stash  # Save work
git checkout feature-b
# Make changes
git add .
git commit -m "Feature B work"

# Back to feature A
git checkout feature-a
git stash pop  # Restore work
```

**Issues:**
❌ Constant branch switching
❌ Need to stash/pop changes
❌ Risk losing work
❌ Can't run tests on feature A while working on feature B

**With Worktrees:**
```bash
# Setup worktrees
git worktree add ~/feature-a feature-a
git worktree add ~/feature-b feature-b

# Terminal 1: Work on feature A
cd ~/feature-a
# Make changes, commit, push

# Terminal 2: Work on feature B (at same time!)
cd ~/feature-b
# Make changes, commit, push

# No switching, no stashing!
```

**Benefits:**
✅ Work on 2 features simultaneously
✅ Run tests on both at same time
✅ No context switching
✅ Keep progress in both

### Use Case 2: Code Review While Implementing

**Scenario:** Implementing feature X, but need to review PR on main

**Without Worktrees:**
```
Working on feature-x
  ↓
PR needs review on main
  ↓
Stash feature-x work
  ↓
Switch to main
  ↓
Review code
  ↓
Comment/approve
  ↓
Switch back to feature-x
  ↓
Pop changes
  ↓
Resume feature-x
```

**With Worktrees:**
```
Terminal 1: Working on feature-x
  ↓ (no interruption)
Terminal 2: Review PR on main
  ↓ (independent)
Back to Terminal 1: Continue feature-x
  ↓ (no context loss)
```

### Use Case 3: Hotfixes on Production

**Scenario:** Production bug needs fixing while developing new feature

**Without Worktrees:**
```
Implementing Feature X (incomplete)
  ↓
Production bug reported
  ↓
Stash Feature X work
  ↓
Checkout main / create hotfix branch
  ↓
Fix bug (high pressure!)
  ↓
Test hotfix
  ↓
Push hotfix
  ↓
Merge hotfix to main
  ↓
Switch back to feature-x
  ↓
Pop changes and continue
```

**With Worktrees:**
```
Terminal 1: Implementing Feature X (no interruption)
  ↓
Terminal 2: Hotfix created immediately
  ↓
Fix bug calmly with full context
  ↓
Test, push, merge
  ↓
Terminal 1: Continue Feature X (never interrupted)
```

### Use Case 4: Comparing Branches

**Scenario:** Need to compare implementation across branches

**Without Worktrees:**
```bash
# Check file on feature-a
git show feature-a:src/MyClass.java > /tmp/feature-a.java

# Check file on feature-b
git show feature-b:src/MyClass.java > /tmp/feature-b.java

# Open in editor
diff /tmp/feature-a.java /tmp/feature-b.java
```

**With Worktrees:**
```bash
git worktree add ~/feature-a feature-a
git worktree add ~/feature-b feature-b

# Open side-by-side in IDE
# Visual comparison is much easier!
```

### Use Case 5: Multiple Test Environments

**Scenario:** Run tests on multiple configurations simultaneously

```
Worktree 1: Java 21, Spring Boot 3.3
  └─ mvn test (Java 21 environment)

Worktree 2: Java 17, Spring Boot 3.2
  └─ mvn test (Java 17 environment)

Both running in parallel!
```

### Use Case 6: Claude Code (Your Current Use Case)

```
Main Repository
    ├─ Main Worktree: /Threadly (branch: main)
    │   └─ Main development continues
    │
    └─ Claude Worktrees (temporary, isolated):
        ├─ agent-a0ef3236 (feature development)
        ├─ agent-ad05d94b (bug fixes)
        ├─ agent-ad522042 (documentation)
        ├─ agent-ae84da91 (implementation)
        └─ agent-ae8e7baf (testing)

Benefits:
✅ Each agent works in isolation
✅ No conflicts with main development
✅ Easy to merge or discard
✅ Main branch never broken
✅ Can work in parallel
```

---

## 🛠️ HOW TO USE GIT WORKTREES

### Creating a Worktree

```bash
# Create new worktree from existing branch
git worktree add <path> <branch>

# Example: Create worktree for feature-a
git worktree add ../feature-a-work feature-a

# Example: Create worktree for new branch
git worktree add ../new-feature new-feature

# Example: Create worktree from remote branch
git worktree add ../main-mirror origin/main
```

### Listing Worktrees

```bash
git worktree list

# Output:
# /path/to/main                   8b9592f [main]
# /path/to/.claude/worktrees/a... 8fd688b [worktree-agent-a0ef3236]
# /path/to/.claude/worktrees/b... 0c89a18 [worktree-agent-ad05d94b]
```

### Using a Worktree

```bash
# Switch to worktree directory
cd /path/to/worktree

# Work normally
git status
git add .
git commit -m "Work on this branch"
git push origin <branch>

# Branch is independent
# Changes don't affect other worktrees
```

### Removing a Worktree

```bash
# Remove worktree (keep branch)
git worktree remove <path>

# Remove worktree and delete branch
git worktree remove --prune <path>

# Example
git worktree remove /path/to/feature-a-work
```

### Cleaning Up

```bash
# List broken worktrees (if path was deleted)
git worktree list --porcelain

# Repair/remove broken worktrees
git worktree prune
```

---

## 📊 WORKTREE COMMANDS REFERENCE

```bash
# Creation
git worktree add <path> <branch>              # Create worktree
git worktree add -b <new-branch> <path> main # Create new branch + worktree
git worktree add <path> --detach <commit>    # Detached HEAD worktree

# Information
git worktree list                             # Show all worktrees
git worktree list --porcelain                 # Machine-readable format
git worktree info <worktree>                  # Info about specific worktree

# Management
git worktree remove <path>                    # Remove worktree
git worktree remove --prune <path>            # Remove + delete branch
git worktree prune                            # Clean up broken references
git worktree lock <path>                      # Lock worktree (prevent removal)
git worktree unlock <path>                    # Unlock worktree

# Repair
git worktree repair                           # Fix broken worktrees
```

---

## ⚠️ WORKTREE BEST PRACTICES

### ✅ DO

```
✅ Use separate terminals for each worktree
   └─ Terminal 1: cd ~/feature-a && work
   └─ Terminal 2: cd ~/feature-b && work

✅ Organize worktree locations
   └─ ~/work/main (primary)
   └─ ~/work/feature-a
   └─ ~/work/feature-b

✅ Use meaningful names
   └─ ~/feature-user-auth (not ~/work1)
   └─ ~/hotfix-payment-bug (not ~/temp)

✅ Remove worktrees when done
   └─ git worktree remove ~/feature-a
   └─ Clean up regularly

✅ Document worktree locations
   └─ README or team wiki
   └─ Script to create standard worktrees
```

### ❌ DON'T

```
❌ Don't move worktree directories
   └─ Git metadata points to specific paths
   └─ Moving breaks references

❌ Don't delete worktree without removing it
   └─ Use: git worktree remove <path>
   └─ Not: rm -rf <path>

❌ Don't commit from multiple worktrees simultaneously
   └─ Risk: merge conflicts
   └─ Solution: use atomic commits

❌ Don't ignore worktree in gitignore
   └─ The .claude/ folder is ignored
   └─ Don't remove that pattern!

❌ Don't share worktrees across machines
   └─ Git metadata is local
   └─ Each machine needs own worktrees
```

---

## 🔧 PRACTICAL EXAMPLE FOR THREADLY

### Setup Script

```bash
#!/bin/bash
# Create development worktrees for Threadly

PROJECT_DIR="/Users/yasva/Kapture/Microservice/Project/Threadly"

# Create feature worktrees
git worktree add $PROJECT_DIR/../feature-identity \
    -b feature/identity-service origin/main

git worktree add $PROJECT_DIR/../feature-integration \
    -b feature/integration-service origin/main

git worktree add $PROJECT_DIR/../feature-billing \
    -b feature/billing-service origin/main

# Create hotfix worktree
git worktree add $PROJECT_DIR/../hotfix-production \
    -b hotfix/production origin/main

echo "✅ Worktrees created:"
git worktree list
```

### Usage Example

```bash
# Terminal 1: Implement identity-service
cd ~/feature-identity
vim services/identity-service/pom.xml
mvn test
git add .
git commit -m "feat: identity-service implementation"
git push origin feature/identity-service

# Terminal 2: Implement integration-service (at same time!)
cd ~/feature-integration
vim services/integration-service/pom.xml
mvn test
git add .
git commit -m "feat: integration-service implementation"
git push origin feature/integration-service

# Terminal 3: Create PR for identity-service
cd ~/Threadly
git checkout main
git pull
gh pr create --base main --head feature/identity-service

# Continue working on both features!
```

### Cleanup

```bash
# After merging feature-identity
cd ~/Threadly
git worktree remove ~/feature-identity
# Worktree deleted
# Branch still exists (merged on main)

# After merging feature-integration
git worktree remove ~/feature-integration
# Worktree deleted
# Branch still exists (merged on main)

# Verify cleanup
git worktree list
# Only main worktree remains
```

---

## 🚀 THREADLY WORKTREE STRUCTURE

### Current Status
```
Main Repository
└─ /Users/yasva/Kapture/Microservice/Project/Threadly
   ├─ .git/ (central repository)
   ├─ .gitignore (includes .claude/)
   ├─ .claude/worktrees/ (IGNORED)
   │  ├─ agent-a0ef3236/ (temporary)
   │  ├─ agent-ad05d94b/ (temporary)
   │  ├─ agent-ad522042/ (temporary)
   │  ├─ agent-ae84da91/ (temporary)
   │  └─ agent-ae8e7baf/ (temporary)
   ├─ services/
   ├─ frontend/
   └─ infra/
```

### Recommended Setup for Parallel Development
```
Threadly Project
└─ /Users/yasva/Kapture/Microservice/Project/Threadly (main)
   └─ .claude/worktrees/ (temporary Claude Code worktrees)

Personal Development Worktrees
└─ ~/threadly-dev/ (organized location)
   ├─ identity-service/ (feature branch)
   ├─ integration-service/ (feature branch)
   ├─ billing-service/ (feature branch)
   └─ hotfix-production/ (hotfix branch)
```

---

## ✅ VERIFICATION CHECKLIST

### Is .claude/worktrees in gitignore?
```bash
git check-ignore -v .claude/
# Output: .gitignore:75:.claude/	.claude/
# ✅ YES, properly ignored
```

### Are worktrees being tracked?
```bash
git status
# Output: nothing added to commit
# ✅ Worktrees NOT tracked
```

### Are worktrees isolated from main?
```bash
git worktree list
# Each has its own branch
# ✅ YES, properly isolated
```

### Can I safely use worktrees?
```bash
# ✅ YES, all conditions met:
# ✅ .claude/ in .gitignore
# ✅ Won't commit worktrees
# ✅ Isolated development
# ✅ Easy to manage
```

---

## 📋 SUMMARY

| Aspect | Status |
|--------|--------|
| **Gitignore Config** | ✅ ACTIVE (.claude/ ignored) |
| **Worktree Safety** | ✅ SAFE (won't commit) |
| **Current Worktrees** | ✅ 5 active (Claude Code) |
| **Best Practice** | ✅ Following recommendations |
| **Need Changes?** | ❌ NO - All good! |

---

## 🎓 WHEN TO USE WORKTREES

| Scenario | Use Worktree? | Why |
|----------|---------------|-----|
| Working on 2 features | ✅ YES | Parallel development |
| Hotfix while implementing | ✅ YES | No context switching |
| Code review of another PR | ✅ YES | Independent branch |
| Testing on multiple branches | ✅ YES | Concurrent testing |
| Simple bug fix | ⚠️ MAYBE | If quick, just switch branch |
| Documentation only | ⚠️ MAYBE | Can commit from main |

---

## 🔗 RELATED RESOURCES

- Git Worktrees Docs: `git worktree --help`
- GitHub Worktrees Guide: https://git-scm.com/docs/git-worktree
- Use Worktree with Multiple Branches: Practice on test repo first

---

**Status:** ✅ YOUR SETUP IS CORRECT & OPTIMIZED

Your worktrees are properly configured, safely ignored, and ready to use for parallel development! 🎉
