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
            } catch (e: Exception) {
                isAuthenticated = false
            }
        }
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
                error = e.localizedMessage ?: "Sign up failed. Please check your network and credentials."
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
                error = e.localizedMessage ?: "Sign in failed. Please check your credentials."
            } finally {
                loading = false
            }
        }
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
