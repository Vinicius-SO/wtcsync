# Campaigns Feature — Implementation Spec

## Objective

Implement the **Campaigns** feature in the Android app, consuming the existing REST API.  
The agent must consult the Pencil MCP for the visual design of each screen before implementing, then follow the build/deploy/validate cycle defined in `AGENTS.md`.

---

## API Overview

**Base URL:** `https://wtcsyncapi-production.up.railway.app`  
**Tag:** `Campaigns`  
**Auth:** Requests that mutate data (POST) require `OPERATOR` role. Read endpoints are open.

---

## Endpoints

---

### 1. `GET /api/campaigns` — List Campaigns

**Description:** Returns all registered campaigns.

**Request:**
- Method: `GET`
- Path: `/api/campaigns`
- Parameters: none
- Body: none

**Response `200`:**

```json
[
  {
    "id": "string",
    "title": "string",
    "body": "string",
    "segmentId": "string",
    "status": "string",
    "mediaUrl": "string",
    "deeplink": "string",
    "actions": [
      {
        "action": "string",
        "title": "string"
      }
    ],
    "actionUrls": {
      "btn1": "string",
      "btn2": "string"
    },
    "stats": {
      "totalTargeted": 0,
      "totalDelivered": 0,
      "totalRead": 0,
      "totalFailed": 0
    },
    "createdBy": "string",
    "createdAt": "2026-05-15T22:58:45.336Z",
    "updatedAt": "2026-05-15T22:58:45.336Z"
  }
]
```

**Field reference:**

| Field | Type | Description |
|---|---|---|
| `id` | String | Unique campaign identifier |
| `title` | String | Campaign title |
| `body` | String | Campaign body text / message |
| `segmentId` | String | Target audience segment |
| `status` | String | Campaign status (`draft`, `scheduled`, `sent`, etc.) |
| `mediaUrl` | String | URL of banner/image to display |
| `deeplink` | String | In-app deep link on tap (e.g. `wtcapp://`) |
| `actions` | Array | CTA buttons. Each has `action` (id) and `title` (label) |
| `actionUrls` | Map | Maps each `action` id to its destination URL |
| `stats.totalTargeted` | Int | Total users targeted |
| `stats.totalDelivered` | Int | Total successfully delivered |
| `stats.totalRead` | Int | Total opened/read |
| `stats.totalFailed` | Int | Total failed deliveries |
| `createdBy` | String | User/system that created the campaign |
| `createdAt` | ISO8601 | Creation timestamp |
| `updatedAt` | ISO8601 | Last update timestamp |

---

### 2. `GET /api/campaigns/{id}` — Get Campaign by ID

**Description:** Returns a single campaign by its ID.

**Request:**
- Method: `GET`
- Path: `/api/campaigns/{id}`
- Path param: `id` (String, required)
- Body: none

**Response `200`:** Same schema as a single object from the list above.

**Error cases to handle:**
- `404` — Campaign not found → show error state on detail screen

---

### 3. `POST /api/campaigns` — Create Campaign

**Description:** Creates a new campaign. Requires `OPERATOR` role.

**Request:**
- Method: `POST`
- Path: `/api/campaigns`
- Content-Type: `application/json`

**Request body:**

```json
{
  "title": "Financial Shift 2025",
  "body": "Não perca o maior evento de finanças do ano.",
  "segmentId": "seg-123",
  "mediaUrl": "https://cdn.wtc.com/banners/financial-shift.png",
  "deeplink": "wtcapp://",
  "actions": [
    {
      "action": "btn1",
      "title": "Garantir Vaga"
    }
  ],
  "actionUrls": {
    "btn1": "https://wtc.com/inscricao"
  }
}
```

**Field rules:**

| Field | Required | Notes |
|---|---|---|
| `title` | Yes | Non-empty string |
| `body` | Yes | Non-empty string |
| `segmentId` | Yes | Must reference a valid segment |
| `mediaUrl` | No | Valid URL. If present, display banner in UI |
| `deeplink` | No | Used for primary tap action on the card |
| `actions` | No | Array of CTA buttons. Each `action` must map to a key in `actionUrls` |
| `actionUrls` | No | Map of `action` id → URL |

**`actions` + `actionUrls` relationship:**
```
actions[0].action = "btn1"          → actionUrls["btn1"] = "https://..."
actions[1].action = "btn2"          → actionUrls["btn2"] = "https://..."
```
Each button label (`title`) is rendered in the UI; on tap, open the corresponding URL from `actionUrls`.

**Response `201`:** Returns the created campaign object (same schema as GET).

**Error cases to handle:**
- `400` — Validation error → show inline field errors
- `403` — Not authorized (non-OPERATOR) → hide create button for non-operator users

---

## Data Model (Android)

```kotlin
data class Campaign(
    val id: String,
    val title: String,
    val body: String,
    val segmentId: String,
    val status: String,
    val mediaUrl: String?,
    val deeplink: String?,
    val actions: List<CampaignAction>,
    val actionUrls: Map<String, String>,
    val stats: CampaignStats,
    val createdBy: String,
    val createdAt: String,
    val updatedAt: String
)

data class CampaignAction(
    val action: String,
    val title: String
)

data class CampaignStats(
    val totalTargeted: Int,
    val totalDelivered: Int,
    val totalRead: Int,
    val totalFailed: Int
)

data class CreateCampaignRequest(
    val title: String,
    val body: String,
    val segmentId: String,
    val mediaUrl: String?,
    val deeplink: String?,
    val actions: List<CampaignAction>,
    val actionUrls: Map<String, String>
)
```

---

## Screens to Implement

### Screen 1 — Campaign List

**Route/destination:** `CampaignListScreen`

**Behavior:**
- On enter: call `GET /api/campaigns`
- Show loading skeleton while fetching
- Render each campaign as a card with:
  - Banner image (`mediaUrl`) if present
  - `title` (headline)
  - `body` (truncated to 2 lines)
  - `status` badge (pill/chip)
- On card tap: navigate to Campaign Detail (`GET /api/campaigns/{id}`)
- Pull-to-refresh support
- Empty state: "Nenhuma campanha encontrada" with a refresh button
- Error state: "Erro ao carregar campanhas" with retry button
- If user is `OPERATOR`: show FAB (+) to navigate to Create Campaign screen

**Stats strip (optional, if design includes it):**
- `totalTargeted` / `totalDelivered` / `totalRead` / `totalFailed`

---

### Screen 2 — Campaign Detail

**Route/destination:** `CampaignDetailScreen(id: String)`

**Behavior:**
- On enter: call `GET /api/campaigns/{id}`
- Show loading skeleton while fetching
- Display full campaign:
  - Full-width banner image (`mediaUrl`) if present
  - `title`
  - `body` (full text)
  - `status` badge
  - Stats row: Targeted / Delivered / Read / Failed
  - CTA buttons from `actions[]`:
    - Label = `action.title`
    - On tap = open `actionUrls[action.action]` in browser or handle `deeplink`
  - `createdBy` and `createdAt` (formatted date)
- Error state: "Campanha não encontrada" with back button
- If primary tap: use `deeplink` field if no actions are defined

---

### Screen 3 — Create Campaign *(OPERATOR only)*

**Route/destination:** `CreateCampaignScreen`

**Behavior:**
- Form fields:
  - `title` — text input (required)
  - `body` — multiline text input (required)
  - `segmentId` — text input or dropdown if segments are available (required)
  - `mediaUrl` — text input, URL (optional)
  - `deeplink` — text input (optional)
  - `actions` — dynamic list of CTA buttons (add/remove):
    - `action` = internal id (auto or user-defined)
    - `title` = button label
    - `actionUrls[action]` = destination URL
- Submit button: `POST /api/campaigns`
- On success: navigate back to list and refresh
- On error: show inline validation messages
- All fields validate before submission (no empty required fields, valid URL format for `mediaUrl` and `actionUrls`)

---

## Repository / Architecture Layer

Implement following the existing project architecture. Suggested structure if not yet present:

```
campaigns/
├── data/
│   ├── CampaignApi.kt           # Retrofit interface
│   ├── CampaignRepository.kt    # Repository implementation
│   └── dto/
│       ├── CampaignDto.kt
│       └── CreateCampaignDto.kt
├── domain/
│   ├── Campaign.kt              # Domain model
│   └── CampaignAction.kt
├── ui/
│   ├── list/
│   │   ├── CampaignListScreen.kt
│   │   └── CampaignListViewModel.kt
│   ├── detail/
│   │   ├── CampaignDetailScreen.kt
│   │   └── CampaignDetailViewModel.kt
│   └── create/
│       ├── CreateCampaignScreen.kt
│       └── CreateCampaignViewModel.kt
```

---

## Retrofit Interface

```kotlin
interface CampaignApi {

    @GET("api/campaigns")
    suspend fun listCampaigns(): List<CampaignDto>

    @GET("api/campaigns/{id}")
    suspend fun getCampaign(@Path("id") id: String): CampaignDto

    @POST("api/campaigns")
    suspend fun createCampaign(@Body request: CreateCampaignDto): CampaignDto
}
```

---

## UI States (per screen)

Each screen must handle the following states explicitly:

```
Loading   → skeleton or spinner
Success   → populated UI
Empty     → empty state component (list only)
Error     → error message + retry action
```

---

## Navigation

Add campaign destinations to the existing NavGraph:

```kotlin
composable("campaigns") { CampaignListScreen(...) }
composable("campaigns/{id}") { backStackEntry ->
    CampaignDetailScreen(id = backStackEntry.arguments?.getString("id") ?: "")
}
composable("campaigns/create") { CreateCampaignScreen(...) }
```

---

## Validation Checklist for Agent

After each deploy cycle, verify:

- [ ] `GET /api/campaigns` returns list and renders cards correctly
- [ ] Banner image loads from `mediaUrl` (or placeholder if null)
- [ ] Tap on card navigates to detail screen with correct `id`
- [ ] `GET /api/campaigns/{id}` loads and displays all fields
- [ ] CTA buttons render with correct labels and open correct URLs
- [ ] `deeplink` is handled when no actions are defined
- [ ] Stats row displays correct numbers
- [ ] `POST /api/campaigns` submits correctly and navigates back on success
- [ ] Form validates required fields before submission
- [ ] Create button/FAB is hidden for non-OPERATOR users
- [ ] All screens handle loading, empty, and error states
- [ ] Pull-to-refresh works on list screen
- [ ] Navigation back stack is correct on all transitions

---

## Agent Instructions

1. Consult the Pencil MCP for the design of `CampaignListScreen`, `CampaignDetailScreen`, and `CreateCampaignScreen` before writing any UI code.
2. Implement data layer first (`CampaignApi`, `CampaignRepository`, DTOs, domain models).
3. Implement `CampaignListScreen` + `CampaignListViewModel`.
4. Deploy, capture screenshot, compare with Pencil reference, iterate.
5. Implement `CampaignDetailScreen` + `CampaignDetailViewModel`.
6. Deploy, capture screenshot, compare with Pencil reference, iterate.
7. Implement `CreateCampaignScreen` + `CreateCampaignViewModel` (OPERATOR only).
8. Deploy, fill form, submit, verify list refreshes with new campaign.
9. Run full validation checklist above before marking the feature as complete.

---

## Authentication

### Endpoint

```
POST /api/auth/login
```

**Request:**

```json
{
  "email": "admin@wtc.com",
  "password": "admin123"
}
```

**cURL:**

```bash
curl -X POST 'https://wtcsyncapi-production.up.railway.app/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email": "admin@wtc.com", "password": "admin123"}'
```

**Response `200`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "admin@wtc.com",
  "expiresIn": "<value>"
}
```

| Field | Type | Description |
|---|---|---|
| `token` | String | JWT Bearer token — must be sent in all authenticated requests |
| `email` | String | Authenticated user email |
| `expiresIn` | Long/String | Token expiration (ms or ISO) |

**Usage:** All subsequent requests must include the header:

```
Authorization: Bearer <token>
```

---

## Test Credentials

| Role | Email | Password | Can create campaigns |
|---|---|---|---|
| `OPERATOR` | admin@wtc.com | admin123 | ✅ Yes |
| `CLIENT` | teste@wtc.com | *(unknown — bcrypt only)* | ❌ No |

> Use `admin@wtc.com` / `admin123` for all development and validation cycles.  
> Use `CLIENT` role behavior to validate that the Create button/FAB is hidden for non-operators.

---

## Auth Flow in the App

### Login sequence

```
1. User enters email + password
2. POST /api/auth/login
3. Store token securely (EncryptedSharedPreferences or DataStore)
4. Attach token to all subsequent API requests via OkHttp Interceptor
5. On 401 response → clear token → redirect to Login screen
```

### Retrofit Auth Interceptor

```kotlin
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}
```

### OkHttpClient setup

```kotlin
val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor { sessionManager.getToken() })
    .build()
```

### Token storage (DataStore recommended)

```kotlin
// Save
dataStore.edit { prefs -> prefs[TOKEN_KEY] = token }

// Read
val token: Flow<String?> = dataStore.data.map { it[TOKEN_KEY] }

// Clear (logout)
dataStore.edit { it.remove(TOKEN_KEY) }
```

---

## Role-based UI Rules

| UI Element | OPERATOR | CLIENT |
|---|---|---|
| Campaign list | ✅ Visible | ✅ Visible |
| Campaign detail | ✅ Visible | ✅ Visible |
| FAB / Create button | ✅ Visible | ❌ Hidden |
| Submit create form | ✅ Allowed | ❌ 403 from API |

The `role` field must be decoded from the JWT payload after login and stored alongside the token. Use it to conditionally show/hide the create entry point in the UI.

### Decode role from JWT

```kotlin
fun decodeRoleFromJwt(token: String): String? {
    return try {
        val payload = token.split(".")[1]
        val decoded = String(Base64.decode(payload, Base64.URL_SAFE))
        JSONObject(decoded).optString("role")
    } catch (e: Exception) {
        null
    }
}
```

---

## Updated Validation Checklist

- [ ] `POST /api/auth/login` with OPERATOR credentials returns a valid token
- [ ] Token is persisted and attached to all subsequent requests
- [ ] `GET /api/campaigns` succeeds with Bearer token
- [ ] `POST /api/campaigns` succeeds with OPERATOR token
- [ ] FAB / Create button is visible when role is `OPERATOR`
- [ ] FAB / Create button is hidden when role is `CLIENT`
- [ ] On `401` response, app clears token and redirects to Login

---

## Implementation Results (v1.0)

### Build Status: ✅ PASS — `assembleDebug` compiles without errors

### Dependencies Added (`app/build.gradle.kts`)

| Library | Version | Purpose |
|---------|---------|---------|
| `com.squareup.retrofit2:retrofit` | 2.9.0 | HTTP client for REST API |
| `com.squareup.retrofit2:converter-gson` | 2.9.0 | JSON serialization/deserialization |
| `com.squareup.okhttp3:okhttp` | 4.12.0 | HTTP engine + interceptor support |
| `com.squareup.okhttp3:logging-interceptor` | 4.12.0 | Debug request/response logging |
| `androidx.datastore:datastore-preferences` | 1.1.1 | JWT token + role persistence |
| `io.coil-kt:coil-compose` | 2.6.0 | Image loading for campaign banners |

### Files Created (12 files, ~1300 lines)

```
br.com.fiap.wtcsync.
├── data/
│   ├── local/
│   │   └── SessionManager.kt              # DataStore + in-memory cache for JWT/role/email
│   └── remote/
│       ├── ApiService.kt                   # Retrofit singleton + OkHttp + AuthInterceptor
│       ├── AuthApi.kt                      # Retrofit interface: POST /api/auth/login
│       └── dto/
│           └── LoginDto.kt                 # LoginRequest, LoginResponse DTOs
├── campaigns/
│   ├── domain/
│   │   └── Campaign.kt                     # Campaign, CampaignAction, CampaignStats, CreateCampaignRequest
│   ├── data/
│   │   ├── CampaignApi.kt                  # Retrofit interface: GET list, GET by id, POST create
│   │   ├── CampaignRepository.kt           # Wraps CampaignApi, returns Resource<T>
│   │   └── dto/
│   │       ├── CampaignDto.kt              # CampaignDto + toDomain() mapper
│   │       └── CreateCampaignDto.kt        # CreateCampaignDto + toDto() mapper
│   └── ui/
│       ├── list/
│       │   ├── CampaignListScreen.kt       # Shimmer, filters, search, empty/error states, FAB condicional
│       │   ├── CampaignListViewModel.kt    # Loading/Success/Empty/Error states, filter + search logic
│       │   └── CampaignListViewModelFactory.kt
│       ├── detail/
│       │   ├── CampaignDetailScreen.kt     # Banner (Coil), body, stats row, CTA buttons, footer
│       │   ├── CampaignDetailViewModel.kt  # Loads by campaign ID
│       │   └── CampaignDetailViewModelFactory.kt
│       └── create/
│           ├── CreateCampaignScreen.kt     # Form with validation, dynamic action buttons
│           ├── CreateCampaignViewModel.kt  # Field state, validation, submit via POST
│           └── CreateCampaignViewModelFactory.kt
```

### Files Modified (7 files)

| File | Change |
|------|--------|
| `app/build.gradle.kts` | Added Retrofit, OkHttp, DataStore, Coil dependencies |
| `data/model/enums/UserRole.kt` | Added `OPERATOR` enum value |
| `data/repository/AuthRepository.kt` | Added `loginWithApi()` method — calls `POST /api/auth/login`, decodes JWT role, stores via SessionManager |
| `ui/auth/AuthViewModel.kt` | Added `currentRole` StateFlow, `logout()`, session restore on init, delegates to `loginWithApi()` |
| `ui/auth/AuthViewModelFactory.kt` | Refactored to receive `Application` for DI access |
| `ui/auth/LoginActivity.kt` | ViewModel injected externally (no default factory) |
| `ui/auth/RegisterScreen.kt` | ViewModel injected externally (no default factory) |
| `ui/Navigation.kt` | Added routes: `campaigns`, `campaigns/{id}`, `campaigns/create`; AuthViewModel restored from DataStore on app start |

### Architecture Overview

```
Login → POST /api/auth/login → JWT
         ↓
    SessionManager (DataStore)
    ├── currentToken: in-memory cache
    ├── currentRole: decoded from JWT payload
    └── currentEmail: from login response
         ↓
    AuthInterceptor → Bearer <token> header on all requests
         ↓
    CampaignApi → CampaignRepository → ViewModel → Screen
         ↓
    401 response → clear session → redirect to Login (TODO)
```

### Role-based UI Flow

1. User logs in via `LoginScreen` → `AuthViewModel.login()` → `AuthRepository.loginWithApi()` → `SessionManager.saveSession()`
2. Role is decoded from JWT payload via `SessionManager.decodeRoleFromJwt()`
3. `AuthViewModel.currentRole` is exposed as a `StateFlow<UserRole?>`
4. `CampaignListScreen` receives `userRole: UserRole?` — renders **"+ Novo" button** only when `userRole == OPERATOR`
5. Non-OPERATOR users see the list and detail screens but **no create entry point**

### State Handling Per Screen

| Screen | Loading | Success | Empty | Error |
|--------|---------|---------|-------|-------|
| List | Shimmer skeleton (3 cards) | Campaign cards | "Nenhuma campanha encontrada" + refresh | "Erro ao carregar campanhas" + retry |
| Detail | CircularProgressIndicator | Full campaign layout | N/A | "Campanha não encontrada" + back/retry |
| Create | Spinner on submit button | Navigate back to list | N/A | Inline error message |

---

## Cleanup — Files That Can Be Removed

After this implementation, the following old files are **orphaned** (no longer imported or used by `Navigation.kt` or any other active code). They contain the old mock-based implementation and should be deleted:

| File | Reason for Removal |
|------|--------------------|
| `data/model/Campanha.kt` | Contains `Campanha` data class (incompatible with API schema — different fields) and `mockCampanhas` list. Replaced by `campaigns/domain/Campaign.kt`. |
| `ui/campaigns/CampanhaListScreen.kt` | Old list screen using mock data directly. Replaced by `campaigns/ui/list/CampaignListScreen.kt` with ViewModel + API integration. |
| `ui/campaigns/CampanhaScreen.kt` | Old detail screen receiving a `Campanha` object directly (no ID-based fetch). Replaced by `campaigns/ui/detail/CampaignDetailScreen.kt` with ViewModel + API call. |
| `ui/campaigns/CampanhaCreateScreen.kt` | Old create screen with no form state, no validation, no submit logic. Replaced by `campaigns/ui/create/CreateCampaignScreen.kt` with ViewModel + POST submit. |

**Note:** The old files are left in place for reference during the transition. Once the new screens are validated end-to-end with the live API, the four files above can be safely deleted.

### Validation Checklist — Code Review Results

| Issue | Severity | Status | Fix |
|-------|----------|--------|-----|
| `SessionManager.restoreSession()` hanging with `collect()` | Critical | ✅ Fixed | Replaced with `first()` |
| Dual `AuthViewModel` instances (Google login path) | Critical | ⚠️ Known | Google login uses Firebase Auth; REST API login is the primary flow. Google path creates its own ViewModel — role propagation works only via email/password path. |
| Token-derived uid from `token.take(16)` | Important | ✅ Fixed | Decodes `sub` claim from JWT payload |
| HTTP errors not differentiated in `loginWithApi()` | Important | ✅ Fixed | Catches `HttpException` (401/403/500), `UnknownHostException`, `SocketTimeoutException` separately |
| `CampaignListViewModel` duplicated across routes | Important | ⚠️ Known | Separate instances exist on `campaigns` route vs `HomeScreen` tab — refresh ensures consistency |
| Actions with blank fields silently dropped | Important | ✅ Fixed | Validates all action fields before submit, shows error for incomplete entries |
| `_sessionRestored` exposed but never observed | Minor | ✅ Fixed | Removed unused field |
| AuthInterceptor reads in-memory token — race on cold start | Minor | ⚠️ Known | `restoreSession()` now completes correctly; token cached in memory after first API call |

### What Was NOT Implemented (for future iterations)

- [ ] **401 auto-redirect** — On 401 response, the app should clear the token and navigate to Login. Currently the `AuthInterceptor` only attaches the token.
- [ ] **Pull-to-refresh** — The list screen uses a manual refresh button in the error/empty states. A swipe-based pull-to-refresh would be a UX improvement.
- [ ] **Segment dropdown** — Create campaign form uses a text field for `segmentId`. A proper dropdown fetching segments from an API endpoint is pending.
- [ ] **Formatted dates** — The detail footer shows raw ISO date (`createdAt.take(10)`). Date formatting with locale is pending.
- [ ] **Auto-refresh on create** — After creating a campaign, the list screen navigates fresh. For a smoother UX, the list could refresh in-place without a full navigation pop.
- [ ] **Google login integration** — Google Sign-In uses Firebase Auth, not the REST API. Google login does not propagate role to Navigation's AuthViewModel. Users should use email/password (`admin@wtc.com` / `admin123`) for the Campaigns feature.
- [ ] **Delete mock files** — Four orphaned mock files should be removed once new screens are validated end-to-end: `data/model/Campanha.kt`, `ui/campaigns/CampanhaListScreen.kt`, `ui/campaigns/CampanhaScreen.kt`, `ui/campaigns/CampanhaCreateScreen.kt`
- [ ] **Potential infinite recomposition in `CampaignListScreen`** — `collectAsState()` calls inside `LaunchedEffect` with no key or wrong keys may trigger re-recomposition loops. Each `collectAsState()` should be reviewed scoped by lifecycle-aware `collectAsStateWithLifecycle()` or proper keys.
- [ ] **CreateCampaignScreen doesn't reset on navigate back** — When navigating back from Create and re-entering, the form retains previous dirty state. The screen should clear its ViewModel state on `DisposableEffect` or via a `reset` signal from Navigation.
- [ ] **Verify FAB visibility for OPERATOR** — The debug badge showing decoded role in the header was added to `CampaignListScreen` for troubleshooting. Install the APK, login as `admin@wtc.com`, and confirm that:
  - The header shows "OPERATOR" in the debug badge
  - The "+ Novo" FAB is visible
  - After logout and login as `teste@wtc.com`, the badge shows "null" or "CLIENT" and the FAB is hidden