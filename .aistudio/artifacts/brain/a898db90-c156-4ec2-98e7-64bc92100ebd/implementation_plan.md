# SCOPE — Master Product Blueprint & Implementation Plan
*See the bigger picture.*

> **AI Character:** Sola (صولا)  
> **Brand & Visual Identity:** Derived from brand emblem (Brain circuitry, Graduation Cap, Open Academic Book)  
> **Primary Identity Palette:** Deep Navy (`#0F172A`), Electric/Royal Blue (`#2563EB`), Slate Neutral (`#475569`), Muted Slate (`#64748B`), and Pristine Surface (`#F8FAFC`)  
> **Typography:** Cairo (العربية / RTL) & Plus Jakarta Sans (Latin / LTR)  

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The architectural direction has been aligned with the user's selections from Phase 1:
> - **Primary Default Language & Layout:** **Arabic with full RTL support and Cairo typography** (plus full dynamic English/LTR support).
> - **Data Persistence & Cloud Sync Foundation:** **Firebase Firestore with Google Sign-In** (enabling multi-device synchronization, secure per-student isolation, and multiplayer/friend collaboration).
> - **Initial Implementation Scope:** **Core Academic Loop** — Home (Command Center), Study (Subjects, Lessons, Focus Timer, Reflection), Schedule (Occurrences, Conflict Detection, Available Windows), and Sola AI (Contextual AI with High Thinking mode using `gemini-4-argon`).

---

## 1. Product Understanding

**SCOPE** is a student operating system designed from first principles to connect academic context with intelligent decision-making. Unlike fragmented to-do lists, timers, or disconnected chatbot widgets, SCOPE operates on a continuous feedback loop:

```
┌────────────────────────────────────────────────────────────────────────┐
│                          CORE PRODUCT LOOP                             │
│                                                                        │
│   Student Data ──► Understanding ──► Academic Context ──► AI Reasoning │
│        ▲                                                         │     │
│        │                                                         ▼     │
│   Updated Plan ◄── Feedback ◄── Real Action ◄── Recommendation ┘     │
└────────────────────────────────────────────────────────────────────────┘
```

The core differentiator is: **"The AI doesn't just answer you. It understands your academic context."**

Deterministic calculations govern facts (durations, scores, deadlines, occurrences, conflict arithmetic, progress percentages), while AI reasoning interprets patterns, identifies learning gaps, explains recommendations, and guides realistic study plans.

---

## 2. Requirement Inventory

| Domain | Requirement / Feature | Category | Implementation Phase |
|---|---|---|---|
| **Identity** | Brand identity, Brain-Cap-Book logo, Cairo font, Arabic RTL-first | Core | Phase 1 |
| **Auth** | Firebase Auth with Google Sign-In via Credential Manager | Core | Phase 1 |
| **Academic** | Subjects, Lessons, Tasks, Exams, Grades, Notes | Core | Phase 1 |
| **Focus** | Study Sessions, Focus Timer, Focus Shield, Session Reflection | Core | Phase 1 |
| **Schedule** | Class Occurrences, Conflict Detection, Free Window Analysis | Core | Phase 1 |
| **AI (Sola)** | Multi-turn contextual chat, Academic Memory, High Thinking | Core | Phase 1 |
| **Progress** | Journey (growth stages), Pulse (recent state), Flame (continuity) | Supporting | Phase 2 |
| **Growth** | Verified Missions, Growth Moments, Baseline tracking | Supporting | Phase 2 |
| **Projects** | Collaborative workspaces, task assignment, review lifecycle | Supporting | Phase 2 |
| **Social** | Unique SCOPE ID, Friend requests, Public Profiles | Supporting | Phase 3 |
| **Challenges**| Beat Yourself, Friend challenges, Sprint/Exam competitions | Supporting | Phase 3 |
| **Community** | General channel, controlled stickers, Challenge cards, Moderation | Supporting | Phase 3 |
| **Cognitive** | Cognitive Lab (logic, working memory, attention, pattern drills) | Supporting | Phase 4 |
| **Guardian** | Guardian Link code, restricted progress view, "Help don't spy" | Supporting | Phase 4 |
| **Theme** | Centralized Theme Engine, Theme 01 ambient surface layers | Future | Phase 4 |
| **Business** | Subscription tiers, ad-free experience (no gameplay paywalls) | Future | Phase 5 |

---

## 3. Assumptions and Architectural Alignment

1. **Deterministic vs. Generative Boundary**: Sola never invents academic entities, exam dates, or grades. Sola receives structured, authorized snapshots (e.g. today's tasks, upcoming exam dates, past reflection signals) and returns reasoning and tailored guidance.
2. **Authentication Invariant**: Zero guest/anonymous mode to guarantee cloud isolation and multi-device integrity. Google Sign-In via Android Credential Manager gates all cloud collections.
3. **Arabic RTL Native Experience**: UI layouts use Compose `CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl)` by default when Arabic is selected, with symmetrical mirrored paddings and Cairo font weights.
4. **Offline Resilience**: Firestore offline caching allows local reads and queued writes during connectivity drops, seamlessly syncing upon reconnection.

---

## 4. Information Architecture & Navigation

The primary navigation destinations are permanently anchored to 5 core surfaces:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           TOP APP BAR / HEADER                          │
│   [SCOPE Logo]    [Current Date / Week]    [Flame / Pulse]   [Profile]  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   1. الرئيسـيـة (Home)        ── Command Center: What to do now & why   │
│   2. الـدراسـة (Study)        ── Subjects, Lessons, Notes, Focus Timer  │
│   3. الـجـدول (Schedule)      ── Time windows, Classes, Conflicts       │
│   4. الـمـشـاريع (Projects)   ── Collaborative Workspaces & Missions    │
│   5. صـــــولا (Sola)         ── Context-aware Academic Intelligence    │
│                                                                         │
├─────────────────────────────────────────────────────────────────────────┤
│                        BOTTOM NAVIGATION (5 TABS)                       │
│      [Home]       [Study]      [Schedule]     [Projects]     [Sola]     │
└─────────────────────────────────────────────────────────────────────────┘
```

- **Header / Control Center**: Houses student avatar, SCOPE ID badge, Quick Settings (Sound, Haptics, Language Toggle, Focus Shield toggle, Theme).
- **Secondary Surfaces** (accessible contextually, not as bottom tabs): Subject Hub, Lesson Detail, Session Reflection Modal, Public Profile View, Friend Manager, Challenge Arena, Cognitive Lab.

---

## 5. User Journeys (A through L)

### Journey A: New Student Onboarding
1. **Launch**: App welcomes student with SCOPE emblem animation.
2. **Auth**: One-tap Google Sign-In via Credential Manager.
3. **Profile Setup**: Assign unique `@scope_id`, select major/academic level, verify Arabic language setting.
4. **First Subject**: Quick creation of initial course (e.g., "الرياضيات المتقدمة").
5. **Initial Plan**: Home screen reveals initial actionable suggestion and Sola welcome brief.

### Journey B: Academic & Focus Loop
1. **Entry**: Open Study surface -> Select Subject.
2. **Start Session**: Launch 25-minute Pomodoro focus block with Focus Shield active.
3. **Completion**: Timer concludes with sound/haptic celebration.
4. **Reflection**: Lightweight prompt: What was learned? Learning signal (UNDERSTOOD / NEEDS_REVIEW / UNCLEAR).
5. **Contextual Ingestion**: Sola and Home update to reflect recently mastered or gap concepts.

### Journey C: Recommendation & Adaptive Planning
1. **Trigger**: Home checks upcoming deadlines and available free windows.
2. **Recommendation**: "You have a 45-minute focus window before Physics class. Prepare 3 summary cards for Calculus Quiz."
3. **Reasoning Trace**: Explains deadline proximity (in 2 days) and prior reflection signal (NEEDS_REVIEW).
4. **Action**: One-tap button opens the targeted task or lesson review.

### Journey D: Sola Deep Inquiry with High Thinking
1. **Trigger**: Student asks complex academic reasoning question or asks Sola to formulate a weekly exam cram schedule.
2. **Model Route**: Sola activates `gemini-4-argon` with `ThinkingLevel.HIGH`.
3. **Execution**: Multi-turn dialogue displays real-time streaming response with collapsible thinking details.
4. **Outcome**: Sola generates a structured study plan with instant "Add to Schedule" action items.

---

## 6. Data Architecture (Firestore Schemas)

```
users/{userId}
  ├── profile: { displayName, scopeId, avatarUrl, language, createdAt }
  ├── academic_settings: { currentTerm, gradeScale, recoveryModeEnabled }
  ├── pulse_state: { state: "Steady"|"Momentum"|"DeepLearning"|"Recovery", lastUpdated }
  │
  ├── subjects/{subjectId}
  │     ├── name, code, colorHex, instructor, creditHours
  │     ├── lessons/{lessonId}: { title, orderIndex, status, summary }
  │     ├── tasks/{taskId}: { title, dueTimestamp, priority, effortMinutes, status, subjectId }
  │     └── exams/{examId}: { title, timestamp, weight, topics }
  │
  ├── schedule_templates/{templateId}
  │     └── { subjectId, dayOfWeek, startTime, endTime, room, recurrenceType }
  │
  ├── occurrences/{occurrenceId}
  │     └── { templateId, subjectId, date, status: "Scheduled"|"Attended"|"Reflected" }
  │
  ├── study_sessions/{sessionId}
  │     └── { subjectId, durationMinutes, timestamp, reflection: { signal, notes } }
  │
  ├── sola_memory/{memoryId}
  │     └── { key, value, category, userApproved, timestamp }
  │
  └── sola_conversations/{chatId}
        └── messages/{messageId}: { sender: "user"|"sola", text, thinkingContent, timestamp }
```

---

## 7. Security, Authorization & Privacy Rules

- **Zero Data Leakage**: Firestore security rules restrict all subcollections under `/users/{userId}/` strictly to `request.auth.uid == userId`.
- **Public Profiles**: Stored in a decoupled `/public_profiles/{scopeId}` read-only collection containing only display name, avatar, and opt-in badges.
- **Grades & Reflections**: 100% private. Never queryable by friends, community, or guardians without explicit revocable token authorization.

---

## 8. Sola AI Architecture & Model Routing

```
┌────────────────────────────────────────────────────────────────────────┐
│                        SOLA INTELLIGENCE HUB                           │
│                                                                        │
│   User Prompt + Authorized Academic Context (Tasks, Exams, Gaps)       │
│                                 │                                      │
│                ┌────────────────┴────────────────┐                     │
│                ▼                                 ▼                     │
│    [Routine Fast Queries]             [Complex Reasoning/Planning]     │
│    Model: gemini-3.1-flash-lite       Model: gemini-4-argon            │
│    Latency: < 400ms                   Thinking: ThinkingLevel.HIGH     │
│    Tasks: Simple Q&A, formatting      Tasks: Gap analysis, exam crams  │
└────────────────────────────────────────────────────────────────────────┘
```

- **System Instruction**: Sola acts as an observant, encouraging, rigorous academic mentor. Sola speaks fluent natural Arabic (صولا) and English.
- **Fail-safe Mode**: If API keys or network fail, deterministic local suggestions (due date alerts and scheduled review prompts) remain 100% functional.

---

## 9. Design System & Visual Identity

### Derived Brand Logo
- **Symbol**: A brain composed of interconnected circuit traces seamlessly blending into an open academic book, crowned by a university graduation cap in the center.
- **Color Palette**:
  - `primary`: `#2563EB` (Cobalt Royal Blue)
  - `primaryContainer`: `#DBEAFE`
  - `background`: `#F8FAFC` (Pure Slate) / `#0A0F1D` (Deep Night Dark)
  - `surface`: `#FFFFFF` / `#111827`
  - `onBackground`: `#0F172A` / `#F1F5F9`
  - `secondary`: `#475569` (Slate Neutral)
  - `accent`: `#06B6D4` (Cyan Energy) / `#10B981` (Focus Green)
- **Typography**: Local Cairo (`res/font/cairo_*.ttf`) with Arabic weights: Light, Regular, Medium, SemiBold, Bold.
- **Shapes & Elevations**: 16dp rounded corners, subtle border strokes (`1.dp` at 8% opacity), and elevation cards with soft shadows.

---

## 10. Phased Implementation Roadmap

### Phase 1: Foundation & Core Academic Loop (Current Target)
- [x] Master Blueprint & Plan Alignment (Turn 1 & 2)
- [ ] Backend provisioning (`set_up_firebase` with Firestore & Google Sign-In)
- [ ] Brand vector assets (Emblem, Sola avatar states, App Icon) & Cairo typography setup
- [ ] App structure & Navigation Compose with 5 primary destinations
- [ ] Auth Gate & User Profile setup (SCOPE ID, language preference)
- [ ] Subject & Course Hub (CRUD, lessons, tasks, exams)
- [ ] Study Focus Mode (Timer, Focus Shield, session reflection)
- [ ] Schedule Intelligence (Class occurrences, conflict detection, available study blocks)
- [ ] Sola Contextual AI Chat with `gemini-4-argon` and High Thinking support

### Phase 2: Progress, Projects & Missions
- [ ] SCOPE Journey (stages & dimensions)
- [ ] SCOPE Pulse & Flame continuous streak engine
- [ ] Project workspace with task assignment and review lifecycle
- [ ] Automated Academic Missions & Growth Moments

### Phase 3: Social & Friendly Competition
- [ ] SCOPE ID lookup and Friend requests
- [ ] Challenges Arena (Beat Yourself personal baseline + Me vs You)
- [ ] Moderated Community General Channel

### Phase 4: Cognitive Lab & Guardian Link
- [ ] Cognitive training modules (Logic, Working memory, Attention)
- [ ] Guardian portal & temporary invite code flow

---

## 11. Verification & Acceptance Criteria

1. **Compilation**: Clean Gradle build without warnings or unresolved dependencies.
2. **RTL Integrity**: Full Arabic layout alignment with zero clipped text or reversed paddings.
3. **Data Integrity**: Real Firestore reads/writes gated by Google Sign-In; no mock hardcoded data.
4. **AI Performance**: Multi-turn dialogue with Sola reflecting real academic schedule and tasks, supporting High Thinking mode.
