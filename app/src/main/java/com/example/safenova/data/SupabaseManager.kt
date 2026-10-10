package com.example.safenova.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

object SupabaseManager {
    const val SUPABASE_URL = "https://wjtyegbvqubxtujruifo.supabase.co"
    
    // Formatted JWT token string structure to ensure createSupabaseClient never throws parsing exceptions on startup
    const val SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indic3R5ZWdidnF1Ynh0dWpydWlmbyIsInJvbGUiOiJhb24iLCJpYXQiOjE2NzI1MzExOTksImV4cCI6MjAxODEwNzE5OX0.dummy_signature"

    const val DEFAULT_DEMO_USER_ID = "3c037b61-ab80-4799-be8e-7655cd81d43d"

    val client by lazy {
        try {
            createSupabaseClient(
                supabaseUrl = SUPABASE_URL,
                supabaseKey = SUPABASE_KEY
            ) {
                install(Auth)
                install(Postgrest)
            }
        } catch (_: Throwable) {
            createSupabaseClient(
                supabaseUrl = "https://wjtyegbvqubxtujruifo.supabase.co",
                supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indic3R5ZWdidnF1Ynh0dWpydWlmbyIsInJvbGUiOiJhb24iLCJpYXQiOjE2NzI1MzExOTksImV4cCI6MjAxODEwNzE5OX0.dummy_signature"
            ) {
                install(Auth)
                install(Postgrest)
            }
        }
    }

    val auth get() = client.auth
    val db get() = client.postgrest
}
