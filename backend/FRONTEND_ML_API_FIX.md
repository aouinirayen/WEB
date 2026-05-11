# Frontend Updates Required

## ⚠️ ML API Path Fix

**Issue Found:** Angular frontend is calling wrong ML endpoint path

---

## 🔴 WRONG Path Used

```typescript
// Currently in the code:
GET http://localhost:4200/api/v1/ml/recommend/18  ❌ Returns 404
```

---

## ✅ CORRECT Path to Use

```typescript
// After backend fix:
GET http://localhost:4200/api/ml/recommend/18  ✅ Returns 200 OK
```

---

## 📝 Files to Update

### Find and Replace

Search in your Angular project for:
- `/api/v1/ml/` 

Replace with:
- `/api/ml/`

### Locations to Check

```typescript
// 1. ml.service.ts (or similar service file)
http.get('/api/ml/recommend/' + userId)      // ✅ Correct
http.get('/api/ml/churn/' + userId)          // ✅ Correct

// 2. Any component using ML API
this.mlService.recommend(userId)             // ✅ Correct
this.mlService.churn(userId)                 // ✅ Correct

// 3. environment.ts (if API URL is configurable)
apiUrl: 'http://localhost:4200'
mlEndpoint: '/api/ml'                        // ✅ Correct
```

---

## 📚 Backend ML Endpoints Available

All endpoints require `Authorization: Bearer <JWT_TOKEN>` header

### Endpoint 1: Plan Recommendation
```bash
GET /api/ml/recommend/{passengerId}

Response: 200 OK
{
  "recommendedPlan": "Premium",
  "score": 85.5,
  "reason": "High travel frequency detected"
}
```

### Endpoint 2: Churn Prediction
```bash
GET /api/ml/churn/{passengerId}

Response: 200 OK
{
  "churnRisk": 0.23,
  "riskLevel": "LOW",
  "message": "User is likely to remain active"
}
```

---

## 🔐 Authorization Required

All ML endpoints require:
```typescript
// Must send JWT token in header
headers: {
  'Authorization': 'Bearer ' + jwtToken,
  'Content-Type': 'application/json'
}
```

**Missing token → 401 Unauthorized**  
**Invalid token → 401 Unauthorized**  
**Expired token → Will auto-refresh, then return result**

---

## 📋 Updated Service Interface

### ml.service.ts
```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class MLService {
  private apiUrl = 'http://localhost:8081/api/ml';  // Updated path

  constructor(private http: HttpClient) { }

  // Get JWT token from auth service
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('jwtToken');
    return new HttpHeaders({
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json'
    });
  }

  // ✅ Recommend plan for passenger
  recommend(passengerId: number): Observable<any> {
    return this.http.get(
      `${this.apiUrl}/recommend/${passengerId}`,
      { headers: this.getHeaders() }
    );
  }

  // ✅ Predict churn for passenger
  predictChurn(passengerId: number): Observable<any> {
    return this.http.get(
      `${this.apiUrl}/churn/${passengerId}`,
      { headers: this.getHeaders() }
    );
  }
}
```

---

## 🧪 Test in Angular Console

```typescript
// In browser console or component:
const userId = 18;
const token = localStorage.getItem('jwtToken');

// Test recommend endpoint
fetch('http://localhost:8081/api/ml/recommend/' + userId, {
  headers: {
    'Authorization': 'Bearer ' + token
  }
})
.then(r => r.json())
.then(data => console.log('✅ ML Recommend:', data))
.catch(e => console.error('❌ Error:', e));

// Test churn endpoint
fetch('http://localhost:8081/api/ml/churn/' + userId, {
  headers: {
    'Authorization': 'Bearer ' + token
  }
})
.then(r => r.json())
.then(data => console.log('✅ ML Churn:', data))
.catch(e => console.error('❌ Error:', e));
```

---

## ✅ Verification Checklist

- [ ] Search codebase for `/api/v1/ml/` - should find **0 results**
- [ ] All ML API calls use `/api/ml/` - should find **2+ results**
- [ ] JWT token is sent in Authorization header
- [ ] Backend Spring Boot is running on `http://localhost:8081`
- [ ] Browser cache is cleared (Ctrl+Shift+Delete)
- [ ] Angular app is on `http://localhost:4200`
- [ ] ML endpoints return 200 OK in browser console

---

## 🚀 After Making Changes

1. **Stop Angular development server** (Ctrl+C)
2. **Open terminal in Angular project folder**
3. **Run:** `npm install` (if dependencies changed)
4. **Run:** `ng serve` (restart development server)
5. **Open browser:** `http://localhost:4200`
6. **Test ML endpoints:** They should now work ✅

---

## 📊 Expected Results

| Scenario | Before Fix | After Fix |
|----------|-----------|-----------|
| ML recommend endpoint | 404 Not Found | 200 OK |
| ML churn endpoint | 404 Not Found | 200 OK |
| Error in browser console | "Failed to load resource: 404" | Returns JSON response |
| User experience | Recommendation feature broken | Recommendation feature works |

---

## 🆘 If Still Getting 404

1. **Check Java backend is running**
   ```bash
   curl http://localhost:8081/api/ml/recommend/18 -H "Authorization: Bearer <token>"
   ```

2. **Check Angular proxy configuration** (if using proxy)
   - File: `angular.json` or `proxy.conf.json`
   - Should route to: `http://localhost:8081`

3. **Check network tab in DevTools**
   - Verify request is going to: `http://localhost:8081/api/ml/...`
   - Not: `http://localhost:4200/api/v1/ml/...`

---

**Updated:** 2026-04-28  
**Component:** ML Service  
**Change Type:** Path Fix (API URL update)  
**Effort:** ~5 minutes implementation

