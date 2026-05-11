# 🎯 QUICK START - Copy/Paste Commands

**Fastest way to get everything working: ~5 minutes**

---

## Step 1: Stop Running Processes

```powershell
# Stop any existing Spring Boot on port 8081
netstat -ano | findstr :8081
# Copy the PID number from output, then:
taskkill /PID <PID_NUMBER> /F

# Example if PID is 28456:
taskkill /PID 28456 /F
```

---

## Step 2: Rebuild Backend

```powershell
cd C:\PIDEV
mvn clean package -DskipTests -q
```

**Expected output:** Just returns to prompt without errors ✅

---

## Step 3: Start Spring Boot

**Option A: IntelliJ IDE**
- Click green "Run" button (top right)
- OR press `Shift + F10`
- Wait 30-60 seconds for startup

**Option B: Command Line**
```powershell
cd C:\PIDEV
java -jar target\PIDEV-0.0.1-SNAPSHOT.jar
```

**Expected output:**
```
Started PidevApplication in X seconds
Tomcat initialized with port 8081
```

---

## Step 4: Test in Browser Console

1. Open browser: `http://localhost:4200`
2. Login to app
3. Press `F12` to open Developer Tools
4. Go to "Console" tab
5. Paste these commands one by one:

```javascript
// Get your JWT token
const token = localStorage.getItem('jwtToken');
console.log('Token:', token.substring(0, 20) + '...');

// Test subscriptions
fetch('http://localhost:8081/api/subscriptions/operator/18', {
  headers: { 'Authorization': 'Bearer ' + token }
})
.then(r => r.json())
.then(d => console.log('✅ Subscriptions OK:', d))
.catch(e => console.error('❌ Error:', e));

// Test loyalty accounts
fetch('http://localhost:8081/api/loyalty-accounts/passenger/18', {
  headers: { 'Authorization': 'Bearer ' + token }
})
.then(r => r.json())
.then(d => console.log('✅ Loyalty OK:', d))
.catch(e => console.error('❌ Error:', e));

// Test payment
fetch('http://localhost:8081/api/payment/initiate/me', {
  method: 'POST',
  headers: { 
    'Authorization': 'Bearer ' + token,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({ amount: 29.99, currency: 'USD' })
})
.then(r => r.json())
.then(d => console.log('✅ Payment OK:', d))
.catch(e => console.error('❌ Error:', e));

// Test ML
fetch('http://localhost:8081/api/ml/recommend/18', {
  headers: { 'Authorization': 'Bearer ' + token }
})
.then(r => r.json())
.then(d => console.log('✅ ML OK:', d))
.catch(e => console.error('❌ Error:', e));
```

**Expected:** All console logs show ✅ messages (no ❌ errors)

---

## Step 5: Update Frontend ML Paths (Only if using ML)

**Search and replace:**
```bash
# In Visual Studio Code or any text editor
Find:    /api/v1/ml/
Replace: /api/ml/

# In terminal:
cd path/to/angular/project
grep -r "/api/v1/ml" src/
```

**Then restart Angular:**
```bash
# Stop Angular (Ctrl+C)
# Restart:
ng serve
```

---

## ✅ Verification

```bash
# Check backend is running
curl http://localhost:8081/actuator/health

# Should return:
# {"status":"UP"}

# Check specific endpoint (replace TOKEN with real token)
curl -H "Authorization: Bearer TOKEN" \
  http://localhost:8081/api/subscriptions/operator/18

# Should return 200 OK with JSON data
```

---

## 🆘 Emergency Restart

**If something goes wrong:**

```bash
# Kill all Java processes
taskkill /F /IM java.exe

# Kill on specific port
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Full clean rebuild
cd C:\PIDEV
mvn clean package -DskipTests -DskipITs

# Clear any build cache
rm -Recurse -Force target\
rm -Recurse -Force .m2\

# Then rebuild
mvn clean install -DskipTests
```

---

## 📱 Test API with cURL

```bash
# Set your variables
$TOKEN = "your_jwt_token"
$BASE = "http://localhost:8081"

# Test 1: Subscriptions
curl -X GET "$BASE/api/subscriptions/operator/18" `
  -H "Authorization: Bearer $TOKEN"

# Test 2: Loyalty
curl -X GET "$BASE/api/loyalty-accounts/passenger/18" `
  -H "Authorization: Bearer $TOKEN"

# Test 3: Payment
curl -X POST "$BASE/api/payment/initiate/me" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json" `
  -d '{"amount": 29.99, "currency": "USD"}'

# Test 4: ML
curl -X GET "$BASE/api/ml/recommend/18" `
  -H "Authorization: Bearer $TOKEN"
```

---

## 🔍 Monitor Logs

```bash
# In IntelliJ, go to: Run → Edit Configurations
# Or monitor console output in real-time

# Or from command line:
java -jar target\PIDEV-0.0.1-SNAPSHOT.jar 2>&1 | Tee-Object -FilePath app.log

# Watch logs in real-time:
Get-Content app.log -Wait
```

---

## 📋 Common Issues & Quick Fixes

```bash
# Issue: Port 8081 already in use
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Issue: Can't find JAR file
cd C:\PIDEV
mvn package -DskipTests

# Issue: Database connection error
# Check application.properties database URL and credentials

# Issue: Angular still showing 404 for ML
grep -rn "/api/v1/ml" src/app/**/*.ts
# Replace all matches with /api/ml/

# Issue: 401 Unauthorized on all endpoints  
localStorage.getItem('jwtToken')
# If empty, login again to get new token

# Issue: CORS error
# Check if Angular proxy is configured correctly
cat angular.json
# Should proxy /api to http://localhost:8081
```

---

## ⏱️ Timing

```
Step 1 (stop processes):      1 minute
Step 2 (rebuild):              2-3 minutes  
Step 3 (start):                1 minute
Step 4 (test):                 1 minute
Step 5 (frontend ML):          2 minutes (if needed)
────────────────────────────
TOTAL:                         7-10 minutes
```

---

## ✅ Success Indicators

```
✅ Port 8081 listening
   netstat -ano | findstr :8081
   → Should show LISTENING

✅ Spring Boot started
   IntelliJ console shows: "Started PidevApplication"

✅ Database connected
   Application starts without "Connection refused" errors

✅ API endpoints working
   Browser console shows: "✅ Subscriptions OK"

✅ Frontend updates done
   No 404 errors for ML endpoints

✅ All systems go
   Dashboard displays user data without errors
```

---

## 🚀 GO-LIVE Checklist

```
☐ Backend compiled successfully
☐ Spring Boot running on port 8081
☐ All 4 test queries return 200 OK
☐ ML paths updated in Angular
☐ Browser cache cleared
☐ Angular app restarted
☐ No red errors in browser console
☐ Dashboard loads user data
☐ All features functional
☐ Ready for production
```

---

**Expected Result:** All endpoints working, no errors ✅

**If stuck:** See IMPLEMENTATION_CHECKLIST.md for detailed troubleshooting

**Time to complete:** ~10 minutes

---

*Last Updated: 2026-04-28*

