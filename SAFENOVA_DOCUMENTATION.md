# SAFENOVA — Native Android (Kotlin + Jetpack Compose) & Supabase Architecture Manual

---

## 🏛️ 1. Complete Architecture Overview

SafeNova is built using **Clean Architecture** principles and **Unidirectional Data Flow**:

```
UI Layer (Jetpack Compose Screens)
       │
       ▼
ViewModel Layer (AuthViewModel, State Management)
       │
       ▼
Data Layer (SafeNovaRepository, SettingsDataStore)
       │
       ▼
Backend & Hardware (Supabase PostgREST/Auth, GPS, Sensors, MediaRecorder)
```

---

## 📁 2. File-by-File Technical Directory

### 🎨 Theme & Styling
- **`ui/theme/Color.kt`**: Defines the "Linux Cinnamon" Dark Purple-Black palette (`#0F0F14` obsidian background, `#181820` card surfaces, `#B056FF` vibrant purple, `#FF3B30` alert red).
- **`ui/theme/Theme.kt`**: Enforces strict dark mode across all Android components using Material 3 `darkColorScheme`.

### 🚦 Navigation
- **`navigation/Screen.kt`**: Type-safe sealed class defining routes, titles, and icons for all screens.
- **`navigation/NavGraph.kt`**: Central Jetpack Compose `NavHost` router. Controls `TopAppBar`, `NavigationBar`, and authentication gating.

### 📱 UI Screens (`ui/screens/`)
1. **`SplashOnboardingScreen.kt`**: Screen 1 — Logo, interactive 3-card onboarding pager, and location permission trigger.
2. **`LoginScreen.kt`**: Screen 2 — Supabase email/password login and sign-up with guest/demo mode fallback.
3. **`HomeScreen.kt`**: Screen 3 — Dynamic greeting, live location badge, hero emergency SOS button, quick helplines (112, 1091, 102), and AI safety score preview.
4. **`SosActiveScreen.kt`**: Screen 4 — Pulsing emergency alert UI, live GPS stream timer, list of notified contacts, and "I'm Safe" resolve action.
5. **`TrustedContactsScreen.kt`**: Screen 5 — Full CRUD for emergency contacts (Add, View, Delete) synced to Supabase `trusted_contacts`.
6. **`FindSheltersScreen.kt`**: Screen 6 — Verified nearby shelters, police stations, trauma hospitals, and 24x7 pharmacies synced to Supabase `safe_places`.
7. **`EmergencyProfileScreen.kt`**: Screen 7 — Medical profile (Blood group, allergies, conditions, responder notes) synced to Supabase `profiles`.
8. **`MapScreen.kt`**: Screen 8, 9, 13 — Interactive dark map canvas with GPS telemetry, heatmap layer, and route options (**Safest Route** vs **Fastest Route**).
9. **`CheckInSetupScreen.kt`**: Screen 10, 11 — Auto Check-in destination, ETA timer (15, 30, 45, 60 mins), and automated SOS trigger on timer expiry.
10. **`CrimeReportScreen.kt`**: Screen 12 — Anonymous incident report picker with real GPS coordinates saved to Supabase `incident_reports`.
11. **`EscortRideScreen.kt`**: Screen 16, 17, 21 — Pink Police night escort request, in-vehicle selfie check, auto-delete on safe arrival, and Campus Safety mode.
12. **`SettingsScreen.kt`**: Master preferences screen controlling gesture triggers, stealth mode, and account sign-out.

### 🧠 Logic, Repositories & Services
- **`data/SupabaseManager.kt`**: Supabase client initialization (`Auth` and `PostgREST`).
- **`data/repo/SafeNovaRepository.kt`**: Repository executing asynchronous PostgREST database queries.
- **`data/SettingsDataStore.kt`**: Local `DataStore<Preferences>` persisting user settings.
- **`location/LocationClient.kt`**: High-accuracy `FusedLocationProviderClient` GPS abstraction.
- **`sensors/ShakeDetector.kt`**: Accelerometer G-force sensor listener detecting 3 rapid phone shakes.
- **`media/AudioRecorderHelper.kt`**: `MediaRecorder` utility recording ambient audio during active SOS.
- **`services/TrackingForegroundService.kt`**: Persistent Android Foreground Service keeping GPS telemetry alive in the background.
- **`ai/AiSafetyEngine.kt`**: Rule-based safety prediction engine evaluating time of day, crowd level, street lighting, and nearby safe refuges.

---

## 🖥️ 3. Responder Web Dashboard

Located in **`responder-dashboard/`**:
- **`index.html`**: Pure HTML/JS interface fetching active SOS alerts in real time with Google Maps links and resolve actions.
- **`server.js`**: Lightweight Node.js server (`npm start`) proxying PostgREST requests safely on port `8080`.

To start the Responder Web Dashboard locally:
```bash
cd responder-dashboard
npm start
```
Then open `http://localhost:8080` in any web browser!
