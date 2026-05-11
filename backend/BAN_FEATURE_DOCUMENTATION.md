# User Ban/Deactivation Feature - Implementation Guide

## Overview

The user ban/deactivation feature allows admins to temporarily or permanently deactivate user accounts. This feature includes:
- Temporary bans (1, 3, 7, 30 days)
- Permanent bans
- Auto-reactivation of expired temporary bans
- Login validation to prevent deactivated users from accessing the system

## Database Schema Changes

A new column has been added to the `users` table:

```sql
ALTER TABLE users ADD COLUMN inactivated_until DATETIME NULL;
```

- **inactivated_until**: Stores the date and time when the ban expires
  - `NULL` with `enabled = false` → Permanent ban
  - Future datetime with `enabled = false` → Temporary ban (expires at this time)
  - `NULL` with `enabled = true` → User is active (no ban)

## API Endpoints

### 1. Ban User (with duration)

**Endpoint:** `PATCH /api/admin/users/{userId}/ban`

**Request Body:**
```json
{
  "durationDays": 1 | 3 | 7 | 30 | null
}
```

**Logic:**
- `durationDays = null` → Permanent ban (sets `enabled = false`, `inactivatedUntil = null`)
- `durationDays = 1,3,7,30` → Temporary ban (sets `enabled = false`, `inactivatedUntil = now + durationDays`)

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "name": "John Doe",
  "cin": 12345678,
  "role": "PASSENGER",
  "photoContentType": "image/jpeg",
  "createdAt": "2025-04-20T10:30:00",
  "updatedAt": "2026-04-25T14:50:00",
  "enabled": false,
  "inactivatedUntil": "2026-05-02T14:50:00"
}
```

**Example:**
```bash
# Permanent ban
curl -X PATCH http://localhost:8080/api/admin/users/1/ban \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"durationDays": null}'

# 7-day ban
curl -X PATCH http://localhost:8080/api/admin/users/1/ban \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{"durationDays": 7}'
```

### 2. Unban User (Remove Deactivation)

**Endpoint:** `PATCH /api/admin/users/{userId}/unban`

**Request Body:** `{}` (empty)

**Logic:**
- Sets `enabled = true`
- Sets `inactivatedUntil = null`

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "name": "John Doe",
  "cin": 12345678,
  "role": "PASSENGER",
  "photoContentType": "image/jpeg",
  "createdAt": "2025-04-20T10:30:00",
  "updatedAt": "2026-04-25T14:50:00",
  "enabled": true,
  "inactivatedUntil": null
}
```

**Example:**
```bash
curl -X PATCH http://localhost:8080/api/admin/users/1/unban \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{}'
```

## Login Validation

When a user attempts to login, the system now:

1. Checks if the user's `enabled` field is `false`
2. If enabled = false:
   - **Permanent ban**: Rejects login with message "Account is permanently deactivated"
   - **Temporary ban (still active)**: Rejects login with message "Account is deactivated until [date]"
   - **Temporary ban (expired)**: Auto-reactivates the user (sets enabled = true) and allows login

**Error Responses:**
```json
{
  "error": "Account is deactivated until 2026-05-02T14:50:00"
}
```

or

```json
{
  "error": "Account is permanently deactivated"
}
```

## Implementation Details

### Modified Files

1. **Entity (User.java)**
   - Added `inactivatedUntil` field
   - Added getter/setter methods

2. **DTO (UserResponse.java)**
   - Added `inactivatedUntil` field
   - Overloaded constructors for backward compatibility

3. **DTO (BanRequest.java)** - NEW
   - Simple POJO for ban request with `durationDays` field

4. **Service (UserService.java)**
   - Added `banUser(Long id, Integer durationDays)` method
   - Added `unbanUser(Long id)` method

5. **Service (UserServiceImpl.java)**
   - Implemented ban/unban logic
   - Updated `convertToResponse()` to include `inactivatedUntil`

6. **Controller (AdminUserController.java)**
   - Added `/ban` endpoint
   - Added `/unban` endpoint
   - Both require ADMIN role

7. **Auth Service (AuthService.java)**
   - Enhanced `authenticateUser()` method with ban validation
   - Auto-reactivates expired bans

## Frontend Usage

### Ban a User (from table action button)

```javascript
fetch(`/api/admin/users/${userId}/ban`, {
  method: 'PATCH',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    durationDays: 7  // or null for permanent
  })
})
.then(res => res.json())
.then(data => {
  // user.enabled should be false
  // user.inactivatedUntil should be set
  console.log('User banned:', data);
})
```

### Unban a User (from table action button)

```javascript
fetch(`/api/admin/users/${userId}/unban`, {
  method: 'PATCH',
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({})
})
.then(res => res.json())
.then(data => {
  // user.enabled should be true
  // user.inactivatedUntil should be null
  console.log('User unbanned:', data);
})
```

## Security Notes

- Ban/unban endpoints require `ADMIN_ROLE`
- All ban operations are logged
- Ban duration is validated on every login
- Expired bans are automatically lifted

## Testing

### Test Scenario 1: Temporary Ban (7 days)
1. Admin calls `/api/admin/users/1/ban` with `durationDays: 7`
2. User enabled = false, inactivatedUntil = now + 7 days
3. User tries to login → Error: "Account is deactivated until [date]"
4. After 7 days, user tries to login → Success (auto-reactivated)

### Test Scenario 2: Permanent Ban
1. Admin calls `/api/admin/users/1/ban` with `durationDays: null`
2. User enabled = false, inactivatedUntil = null
3. User tries to login → Error: "Account is permanently deactivated"
4. Admin calls `/api/admin/users/1/unban`
5. User tries to login → Success

### Test Scenario 3: Unban Expired Ban
1. Admin bans user for 1 day
2. Time passes (1+ day)
3. User tries to login → Success (auto-reactivated)
4. Check user status → enabled = true, inactivatedUntil = null

## Migration

If you have an existing database:

1. **Option A: Automatic (JPA)**
   - Spring Boot will automatically create the column on startup
   - No action needed if using `spring.jpa.hibernate.ddl-auto=update`

2. **Option B: Manual Migration**
   - Run the migration SQL script: `migration_ban_feature.sql`
   - This adds the `inactivated_until` column and index

## Error Handling

| Scenario | Error Code | Message |
|----------|-----------|---------|
| User not found | 404 | User not found with id: {id} |
| User permanently banned | 401 | Account is permanently deactivated |
| User temporarily banned | 401 | Account is deactivated until {date} |
| Invalid ban duration | 400 | Invalid duration value |

## Future Enhancements

1. Add scheduled task to auto-cleanup expired bans
2. Add ban reason/notes field
3. Add ban history/audit log
4. Add email notifications for bans
5. Add frontend UI for ban management


