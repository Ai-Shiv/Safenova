package com.example.safenova.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

object SupabaseManager {
    const val SUPABASE_URL = "https://wjtyegbvqubxtujruifo.supabase.co"
    const val SUPABASE_KEY = "YOUR_SUPABASE_ANON_KEY_HERE"

    val client by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_KEY
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    val auth get() = client.auth
    val db get() = client.postgrest
}
