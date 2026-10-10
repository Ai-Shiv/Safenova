package com.example.safenova.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safenova.data.SupabaseManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthViewModel : ViewModel() {
    var loading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var isAuthenticated by mutableStateOf(false)
        private set

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            try {
                val session = SupabaseManager.auth.currentSessionOrNull()
                isAuthenticated = session != null
            } catch (_: Exception) {
                isAuthenticated = false
            }
        }
    }

    fun clearError() {
        error = null
    }

    fun signUp(emailInput: String, passwordInput: String, fullName: String) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                SupabaseManager.auth.signUpWith(Email) {
                    email = emailInput
                    password = passwordInput
                    data = buildJsonObject {
                        put("full_name", fullName)
                    }
                }
                isAuthenticated = true
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: e.message ?: "Sign up error"
                if (msg.contains("API", ignoreCase = true) || msg.contains("401") || msg.contains("Unauthorized")) {
                    error = "Supabase Key/Network Error: Please check your Supabase Anon Key in SupabaseManager.kt, or use Demo Mode."
                } else {
                    error = msg
                }
            } finally {
                loading = false
            }
        }
    }

    fun signIn(emailInput: String, passwordInput: String) {
        viewModelScope.launch {
            loading = true
            error = null
            try {
                SupabaseManager.auth.signInWith(Email) {
                    email = emailInput
                    password = passwordInput
                }
                isAuthenticated = true
            } catch (e: Exception) {
                val msg = e.localizedMessage ?: e.message ?: "Sign in error"
                if (msg.contains("API", ignoreCase = true) || msg.contains("401") || msg.contains("Unauthorized")) {
                    error = "Invalid Supabase Key: Please replace SUPABASE_KEY in SupabaseManager.kt with your Project Anon Key (from Settings -> API), or use Demo Mode."
                } else {
                    error = msg
                }
            } finally {
                loading = false
            }
        }
    }

    fun continueAsGuest() {
        error = null
        isAuthenticated = true
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                SupabaseManager.auth.signOut()
            } catch (_: Exception) {}
            isAuthenticated = false
        }
    }
}
