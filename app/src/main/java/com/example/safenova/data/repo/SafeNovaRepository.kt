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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SafeNovaRepository {

    private val db = SupabaseManager.client.postgrest
    private val auth = SupabaseManager.client.auth

    suspend fun getCurrentUserId(): String? = withContext(Dispatchers.IO) {
        auth.currentUserOrNull()?.id
    }

    // Incidents & Crime Reports
    suspend fun submitIncidentReport(report: IncidentReport) = withContext(Dispatchers.IO) {
        db.from("incident_reports").insert(report)
    }

    suspend fun fetchIncidentReports(): List<IncidentReport> = withContext(Dispatchers.IO) {
        db.from("incident_reports").select().decodeList<IncidentReport>()
    }

    // Emergency SOS
    suspend fun triggerSosAlert(lat: Double, lng: Double): SosAlert = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        val alert = SosAlert(
            userId = userId,
            status = "ACTIVE",
            lastLatitude = lat,
            lastLongitude = lng
        )
        db.from("sos_alerts").insert(alert).decodeSingle<SosAlert>()
    }

    suspend fun resolveSosAlert(sosId: String) = withContext(Dispatchers.IO) {
        db.from("sos_alerts").update({
            set("status", "RESOLVED")
        }) {
            filter {
                eq("id", sosId)
            }
        }
    }

    // Area Conditions
    suspend fun submitAreaCondition(condition: AreaCondition) = withContext(Dispatchers.IO) {
        db.from("area_conditions").insert(condition)
    }

    suspend fun fetchAreaConditions(): List<AreaCondition> = withContext(Dispatchers.IO) {
        db.from("area_conditions").select().decodeList<AreaCondition>()
    }

    // Safe Places & Shelters
    suspend fun fetchSafePlaces(): List<SafePlace> = withContext(Dispatchers.IO) {
        db.from("safe_places").select().decodeList<SafePlace>()
    }

    // Trusted Contacts
    suspend fun addTrustedContact(name: String, phone: String, relation: String) = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        val contact = TrustedContact(
            userId = userId,
            contactName = name,
            contactPhone = phone,
            relation = relation
        )
        db.from("trusted_contacts").insert(contact)
    }

    suspend fun fetchTrustedContacts(): List<TrustedContact> = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId() ?: return@withContext emptyList()
        db.from("trusted_contacts").select {
            filter {
                eq("user_id", userId)
            }
        }.decodeList<TrustedContact>()
    }

    // Emergency PIN
    suspend fun updateEmergencyPin(pin: String) = withContext(Dispatchers.IO) {
        val userId = getCurrentUserId() ?: throw IllegalStateException("User not logged in")
        db.from("profiles").update({
            set("emergency_pin", pin)
        }) {
            filter {
                eq("id", userId)
            }
        }
    }

    // Active Journeys
    suspend fun startJourney(journey: ActiveJourney) = withContext(Dispatchers.IO) {
        db.from("active_journeys").insert(journey)
    }
}
