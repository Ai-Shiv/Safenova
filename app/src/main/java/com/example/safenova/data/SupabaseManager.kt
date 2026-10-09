package com.example.safenova.data

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

object SupabaseManager {
    // Replace with your Supabase Project URL from https://supabase.com/dashboard/project/.../settings/api
    const val SUPABASE_URL = "https://wjtyegbvqubxtujruifo.supabase.co"
    
    // Replace with your Supabase Anon API Key from Dashboard -> Settings -> API
    const val SUPABASE_KEY = "sb_secret_mUiFKY4jzLb0LSDkck1GDw_8x-gCVVy"

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
