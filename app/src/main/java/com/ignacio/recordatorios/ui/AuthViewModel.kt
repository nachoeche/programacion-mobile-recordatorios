package com.ignacio.recordatorios.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed interface AuthUiState {
    data object Loading : AuthUiState
    data object SignedOut : AuthUiState
    data class SignedIn(val uid: String, val email: String) : AuthUiState
    data class Error(val message: String) : AuthUiState
}
class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val state: StateFlow<AuthUiState> = _state
    val busy = MutableStateFlow(false)
    private fun stateOf(user: FirebaseUser?): AuthUiState = if(user == null) AuthUiState.SignedOut
        else AuthUiState.SignedIn(user.uid, user.email ?: "")
    private val listener = FirebaseAuth.AuthStateListener { _state.value = stateOf(it.currentUser) }
    init { auth.addAuthStateListener(listener); _state.value = stateOf(auth.currentUser) }
    fun login(email: String, password: String, register: Boolean) {
        if (busy.value || email.isBlank() || password.length < 6) {
            _state.value = AuthUiState.Error("Ingresá un correo y una contraseña de al menos 6 caracteres")
            return
        }
        busy.value = true
        viewModelScope.launch {
            try {
                if (register) auth.createUserWithEmailAndPassword(email.trim(), password).await()
                else auth.signInWithEmailAndPassword(email.trim(), password).await()
                _state.value = stateOf(auth.currentUser)
            } catch (e: CancellationException) { throw e }
            catch(e: Exception) { _state.value = AuthUiState.Error(e.localizedMessage ?: "Error de autenticacion") }
            finally { busy.value = false }
        }
    }
    fun logout() { auth.signOut(); _state.value = AuthUiState.SignedOut }
    override fun onCleared() { auth.removeAuthStateListener(listener); super.onCleared() }
}
