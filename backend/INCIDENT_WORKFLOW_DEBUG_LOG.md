# Incident Creation Workflow Debug Log

This file is a handoff/debug trace for the **incident creation workflow** in the current Spring Boot backend.
It is meant for another AI agent to inspect when something in the workflow does not behave as expected.

## Goal

When an agent creates or updates an incident:
1. The request is accepted by the incident controller.
2. The service validates the authenticated user and role.
3. The incident is saved.
4. The AI incident analyzer runs.
5. Notifications are sent to agents and passengers.
6. The API returns an **agent-only submission response**.

### Visibility rule
- **Public DTO**: `IncidentNotificationDTO`
  - Used for `GET /incidents` and `GET /incidents/get/{id}`.
  - Must **not** expose confidence.
- **Agent-only DTO**: `IncidentSubmissionResponseDTO`
  - Used for `POST /incidents/add` and `PUT /incidents/update/{id}`.
  - May expose `confidencePercent` only to the submitting agent.

## Main code path

- Controller: `src/main/java/tn/esprit/pidev/controller/IncNot/IncidentController.java`
- Service: `src/main/java/tn/esprit/pidev/service/IncNot/IncidentServiceImpl.java`
- AI service: `src/main/java/tn/esprit/pidev/service/IncNot/AIIncidentService.java`
- Notification service: `src/main/java/tn/esprit/pidev/service/IncNot/NotificationServiceImpl.java`
- Email service: `src/main/java/tn/esprit/pidev/service/IncNot/MailingService.java`
- Global error handling: `src/main/java/tn/esprit/pidev/controller/GlobalExceptionHandler.java`

---

## Full workflow trace

### 1) Request enters controller

**Endpoint**
- `POST /incidents/add`
- `PUT /incidents/update/{id}`

**Expected debug trace**
```text
[IncidentController] Received incident create/update request
[IncidentController] Authentication principal detected
[IncidentController] Forwarding request to IncidentService.saveIncident(...)
```

**Important**
- The controller should return `IncidentSubmissionResponseDTO` for create/update.
- The controller should return `IncidentNotificationDTO` for read operations.

---

### 2) Authentication and role validation

**What the service does**
- Finds the user by username.
- Verifies the role is `AGENT`.
- Rejects non-agent users.

**Expected debug trace**
```text
[IncidentServiceImpl] Looking up user by username: <username>
[IncidentServiceImpl] User found: <username>
[IncidentServiceImpl] Validating role: AGENT
[IncidentServiceImpl] Role validation passed
```

**Failure paths**
```text
[IncidentServiceImpl] User not found -> throw InvalidFileException
[IncidentServiceImpl] Role is not AGENT -> throw InvalidFileException
```

---

### 3) Incident persistence

**What the service does**
- Sets `reportedBy` to the authenticated agent.
- Saves the incident entity to the database.

**Expected debug trace**
```text
[IncidentServiceImpl] Setting reportedBy on incident entity
[IncidentServiceImpl] Saving incident to repository
[IncidentServiceImpl] Incident saved with id: <id>
```

**Failure paths**
```text
[IncidentServiceImpl] Repository save failed -> global exception handler or server error
```

---

### 4) AI analysis

**What the service does**
- Sends `title` and `description` to `AIIncidentService.analyzeIncident(...)`.
- Uses the AI API response if available.
- Falls back to a local heuristic if the AI API fails.

**Expected debug trace**
```text
[AIIncidentService] Preparing AI payload
[AIIncidentService] Sending request to AI endpoint: http://localhost:5000/predict
[AIIncidentService] AI response received
[AIIncidentService] Parsed severity / delay / duplicate / confidence fields
```

**Fallback trace**
```text
[AIIncidentService] AI API unavailable, fallback used: <error message>
[AIIncidentService] Using heuristic fallback analysis
```

**Important**
- `confidencePercent` exists only in `AIIncidentAnalysis` and the agent-only submission response.
- It must not be copied into the public incident DTO.

---

### 5) DTO shaping for notifications

**What the service does**
- Builds a public `IncidentNotificationDTO` for notification/email fan-out.
- Sends that DTO to notification services.
- Returns `IncidentSubmissionResponseDTO` only to the submitting agent.

**Expected debug trace**
```text
[IncidentServiceImpl] Building public notification DTO
[IncidentServiceImpl] DTO fields: title, severity, location, reportedByName
[IncidentServiceImpl] Sending notifications to agents
[IncidentServiceImpl] Sending delay notifications to passengers
[IncidentServiceImpl] Building agent-only submission response
```

**Visibility rule**
- `IncidentNotificationDTO` must stay confidence-free.
- `IncidentSubmissionResponseDTO` can carry `confidencePercent`.

---

### 6) Notification fan-out

**Agent notifications**
- Saved internally for all agents.
- Also emailed to agents.

**Passenger notifications**
- Saved internally for all passengers.
- Also emailed to passengers.

**Expected debug trace**
```text
[NotificationServiceImpl] Sending internal notifications to AGENT users
[NotificationServiceImpl] Internal notification saved for agent: <username>
[NotificationServiceImpl] Sending external notifications to AGENT users
[MailingService] Agent email sent to: <email>
[NotificationServiceImpl] Sending delay notifications to PASSENGER users
[NotificationServiceImpl] Internal delay notification saved for passenger: <username>
[MailingService] Passenger email sent to: <email>
```

**Failure paths**
```text
[NotificationServiceImpl] Notification repository save failed
[MailingService] Failed to send agent/passenger email
```

---

### 7) Response returned to client

**Create/update endpoints**
- Return `IncidentSubmissionResponseDTO`
- Contains the current incident summary plus `confidencePercent`

**Get/list endpoints**
- Return `IncidentNotificationDTO`
- Must not include confidence

**Expected debug trace**
```text
[IncidentServiceImpl] Returning agent-only submission response
[IncidentController] Response sent to authenticated agent
```

---

## Current logging behavior in code

The code already logs some parts of the workflow:

### `AIIncidentService`
- Warns when the AI API is unavailable and fallback is used.

### `NotificationServiceImpl`
- Logs when agent/passenger notifications are saved.
- Logs when emails are sent or fail.

### `GlobalExceptionHandler`
- Logs validation errors.
- Logs unexpected errors.
- Logs missing document-related errors.

---

## Failure points an agent should inspect first

1. **Authentication principal is missing**
   - Controller depends on `Authentication authentication`.
   - Check security context / JWT configuration.

2. **Username not found**
   - `userRepository.findByUsername(agentUsername)` returns empty.

3. **Role mismatch**
   - Only `RoleEnum.AGENT` is allowed to submit incidents.

4. **AI service unavailable**
   - Fallback heuristic will be used.
   - Confidence still comes from AI analysis object and submission response.

5. **Notification/email failures**
   - Check mail settings and notification repository writes.

6. **DTO leakage**
   - Confidence must not appear in `IncidentNotificationDTO`.
   - If it does, the public API is leaking agent-only data.

---

## Expected verification checklist

- [ ] `POST /incidents/add` returns `IncidentSubmissionResponseDTO`
- [ ] `PUT /incidents/update/{id}` returns `IncidentSubmissionResponseDTO`
- [ ] `GET /incidents` returns `IncidentNotificationDTO`
- [ ] `GET /incidents/get/{id}` returns `IncidentNotificationDTO`
- [ ] `IncidentNotificationDTO` does **not** contain confidence
- [ ] `IncidentSubmissionResponseDTO` does contain confidence
- [ ] AI fallback still works if the external AI endpoint is down
- [ ] Notification fan-out still runs after saving the incident

---

## Short handoff summary

If something breaks, inspect this order:
1. Controller return types
2. `IncidentServiceImpl.saveIncident(...)`
3. `AIIncidentService.analyzeIncident(...)`
4. `NotificationServiceImpl`
5. `GlobalExceptionHandler`

The main rule is simple: **confidence is agent-only after submission, never public in read DTOs**.

