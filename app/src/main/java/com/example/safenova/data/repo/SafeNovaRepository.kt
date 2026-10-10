package com.example.safenova.data.repo

import com.example.safenova.data.SupabaseManager
import com.example.safenova.data.models.ActiveJourney
import com.example.safenova.data.models.AreaCondition
import com.example.safenova.data.models.IncidentReport
import com.example.safenova.data.models.SafePlace
import com.example.safenova.data.models.SosAlert
import com.example.safenova.data.models.TrustedContact
import com.example.safenova.data.models.UserProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant

class SafeNovaRepository {

    private val db = SupabaseManager.client.postgrest
    private val auth = SupabaseManager.client.auth

    suspend fun getCurrentUserId(): String? = withContext(Dispatchers.IO) {
        auth.currentUserOrNull()?.id
    }

    suspend fun getEffectiveUserId(): String = withContext(Dispatchers.IO) {
        auth.currentUserOrNull()?.id ?: SupabaseManager.DEFAULT_DEMO_USER_ID
    }

    suspend fun ensureProfileExists(userId: String, fullName: String, phone: String? = null) = withContext(Dispatchers.IO) {
        try {
            val existing = db.from("profiles").select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<UserProfile>()
            if (existing == null) {
                db.from("profiles").upsert(
                    UserProfile(
                        id = userId,
                        fullName = fullName.ifBlank { "SafeNova User" },
                        phoneNumber = phone,
                        emergencyPin = "2580|O+|None|None|Notify trusted contacts immediately"
                    )
                )
            }
        } catch (_: Exception) {}
    }

    // Incidents & Community Reports
    suspend fun submitIncidentReport(report: IncidentReport): IncidentReport? = withContext(Dispatchers.IO) {
        val uid = getEffectiveUserId()
        val payload = report.copy(reporterId = uid)
        db.from("incident_reports").insert(payload) {
            select()
        }.decodeSingleOrNull<IncidentReport>()
    }

    suspend fun fetchIncidentReports(): List<IncidentReport> = withContext(Dispatchers.IO) {
        db.from("incident_reports").select {
            order("created_at", Order.DESCENDING)
        }.decodeList<IncidentReport>()
    }

    // Emergency SOS Alerts
    suspend fun triggerSosAlert(lat: Double, lng: Double, modeNote: String = "LIVE_SOS_STREAM"): SosAlert = withContext(Dispatchers.IO) {
        val userId = getEffectiveUserId()
        val alert = SosAlert(
            userId = userId,
            status = "ACTIVE",
            lastLatitude = lat,
            lastLongitude = lng,
            audioUrl = modeNote
        )
        db.from("sos_alerts").insert(alert) {
            select()
        }.decodeSingle<SosAlert>()
    }

    suspend fun updateSosLocation(sosId: String, lat: Double, lng: Double) = withContext(Dispatchers.IO) {
        db.from("sos_alerts").update({
            set("last_latitude", lat)
            set("last_longitude", lng)
        }) {
            filter { eq("id", sosId) }
        }
    }

    suspend fun resolveSosAlert(sosId: String, finalStatus: String = "RESOLVED") = withContext(Dispatchers.IO) {
        db.from("sos_alerts").update({
            set("status", finalStatus)
            set("resolved_at", Instant.now().toString())
        }) {
            filter { eq("id", sosId) }
        }
    }

    suspend fun fetchActiveSosAlerts(): List<SosAlert> = withContext(Dispatchers.IO) {
        db.from("sos_alerts").select {
            order("created_at", Order.DESCENDING)
        }.decodeList<SosAlert>()
    }

    // Area Conditions
    suspend fun submitAreaCondition(condition: AreaCondition) = withContext(Dispatchers.IO) {
        val uid = getEffectiveUserId()
        db.from("area_conditions").insert(condition.copy(reporterId = uid))
    }

    suspend fun fetchAreaConditions(): List<AreaCondition> = withContext(Dispatchers.IO) {
        db.from("area_conditions").select {
            order("created_at", Order.DESCENDING)
        }.decodeList<AreaCondition>()
    }

    // Safe Places, Nearby Help (Police, Hospitals, Pharmacies) & Shelters
    suspend fun fetchSafePlaces(): List<SafePlace> = withContext(Dispatchers.IO) {
        db.from("safe_places").select().decodeList<SafePlace>()
    }

    suspend fun addSafePlace(place: SafePlace): SafePlace? = withContext(Dispatchers.IO) {
        db.from("safe_places").insert(place) {
            select()
        }.decodeSingleOrNull<SafePlace>()
    }

    // User Profile & Emergency Medical Info
    suspend fun fetchUserProfile(): UserProfile? = withContext(Dispatchers.IO) {
        val userId = getEffectiveUserId()
        try {
            val raw = db.from("profiles").select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<UserProfile>()
            raw?.unpackMedicalMetadata()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        val uid = profile.id.ifBlank { getEffectiveUserId() }
        val packed = profile.copy(id = uid).packForDatabase()
        db.from("profiles").upsert(packed)
    }

    // Trusted Contacts Full CRUD
    suspend fun addTrustedContact(name: String, phone: String, relation: String): TrustedContact? = withContext(Dispatchers.IO) {
        val userId = getEffectiveUserId()
        val contact = TrustedContact(
            userId = userId,
            contactName = name,
            contactPhone = phone,
            relation = relation,
            isActive = true
        )
        db.from("trusted_contacts").insert(contact) {
            select()
        }.decodeSingleOrNull<TrustedContact>()
    }

    suspend fun updateTrustedContact(
        contactId: String,
        name: String,
        phone: String,
        relation: String,
        isActive: Boolean
    ) = withContext(Dispatchers.IO) {
        db.from("trusted_contacts").update({
            set("contact_name", name)
            set("contact_phone", phone)
            set("relation", relation)
            set("is_active", isActive)
        }) {
            filter { eq("id", contactId) }
        }
    }

    suspend fun deleteTrustedContact(contactId: String) = withContext(Dispatchers.IO) {
        db.from("trusted_contacts").delete {
            filter { eq("id", contactId) }
        }
    }

    suspend fun fetchTrustedContacts(): List<TrustedContact> = withContext(Dispatchers.IO) {
        val userId = getEffectiveUserId()
        db.from("trusted_contacts").select {
            filter { eq("user_id", userId) }
            order("created_at", Order.ASCENDING)
        }.decodeList<TrustedContact>()
    }

    // Emergency Duress PIN
    suspend fun updateEmergencyPin(pin: String) = withContext(Dispatchers.IO) {
        val current = fetchUserProfile()
        if (current != null) {
            updateUserProfile(current.copy(emergencyPin = pin))
        } else {
            val uid = getEffectiveUserId()
            db.from("profiles").update({
                set("emergency_pin", "$pin|O+|None|None|Duress PIN configured")
            }) {
                filter { eq("id", uid) }
            }
        }
    }

    // Active Journeys & Auto Check-In
    suspend fun startJourney(destLat: Double, destLng: Double, etaMinutes: Int): ActiveJourney? = withContext(Dispatchers.IO) {
        val userId = getEffectiveUserId()
        val etaIso = Instant.now().plusSeconds(etaMinutes * 60L).toString()
        val journey = ActiveJourney(
            userId = userId,
            destinationLat = destLat,
            destinationLng = destLng,
            expectedArrivalTime = etaIso,
            status = "ACTIVE"
        )
        db.from("active_journeys").insert(journey) {
            select()
        }.decodeSingleOrNull<ActiveJourney>()
    }

    suspend fun completeJourney(journeyId: String, finalStatus: String = "SAFE_ARRIVAL") = withContext(Dispatchers.IO) {
        db.from("active_journeys").update({
            set("status", finalStatus)
        }) {
            filter { eq("id", journeyId) }
        }
    }

    suspend fun fetchActiveJourneys(): List<ActiveJourney> = withContext(Dispatchers.IO) {
        val userId = getEffectiveUserId()
        db.from("active_journeys").select {
            filter { eq("user_id", userId) }
            order("created_at", Order.DESCENDING)
        }.decodeList<ActiveJourney>()
    }
}
