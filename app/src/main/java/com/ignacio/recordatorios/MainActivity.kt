package com.ignacio.recordatorios

import android.os.Bundle
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.ignacio.recordatorios.data.RepositoryProvider
import com.ignacio.recordatorios.ui.*
import com.ignacio.recordatorios.ui.theme.RecordatoriosTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var connection: ConnectivityManager? = null
    private var callback: ConnectivityManager.NetworkCallback? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        connection = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
                lifecycleScope.launch { RepositoryProvider.repository(applicationContext, uid).sync() }
            }
        }.also { connection?.registerDefaultNetworkCallback(it) }
        setContent {
            val authVm: AuthViewModel = viewModel()
            val auth by authVm.state.collectAsStateWithLifecycle()
            when (val a = auth) {
                AuthUiState.Loading -> CircularProgressIndicator()
                AuthUiState.SignedOut -> AuthScreen(authVm)
                is AuthUiState.Error -> AuthScreen(authVm, a.message)
                is AuthUiState.SignedIn -> key(a.uid) {
                    // A new user gets a new navigation stack and new ViewModels.
                    // The Room database is also isolated by UID.
                    UserReminderApp(a.uid, a.email, authVm)
                }
            }
        }
    }
    override fun onDestroy() {
        callback?.let { connection?.unregisterNetworkCallback(it) }
        super.onDestroy()
    }
    @Composable private fun UserReminderApp(uid: String, email: String, authVm: AuthViewModel) {
        val nav = rememberNavController()
        val repo = remember(uid) { RepositoryProvider.repository(applicationContext, uid) }
        val prefs = remember { RepositoryProvider.preferences(applicationContext) }
        val settingsVm: SettingsViewModel = viewModel(key = "settings-$uid", factory = SettingsViewModel.factory(prefs))
        val options by settingsVm.state.collectAsStateWithLifecycle()
        val listVm: ReminderListViewModel = viewModel(key = "list-$uid", factory = ReminderListViewModel.factory(repo, prefs))
        LaunchedEffect(uid) { repo.sync() }
        RecordatoriosTheme(darkTheme = options.darkMode) {
            NavHost(navController = nav, startDestination = "list") {
                composable("list") {
                    androidx.compose.foundation.layout.Column {
                        androidx.compose.material3.Text("Cuenta: $email")
                        androidx.compose.material3.TextButton(onClick = authVm::logout) { androidx.compose.material3.Text("Cerrar sesión") }
                        ReminderScreen(listVm, onCrearRecordatorio = { nav.navigate("create") },
                            onRecordatorioClick = { nav.navigate("detail/$it") }, onSettings = { nav.navigate("settings") })
                    }
                }
                composable("settings") { SettingsScreen(settingsVm) { nav.popBackStack() } }
                composable("create") { entry ->
                    val vm: CreateReminderViewModel = viewModel(viewModelStoreOwner = entry,
                        factory = CreateReminderViewModel.factory(repo))
                    CreateReminderScreen(vm, onGuardar = { nav.popBackStack() }, onCancelar = { nav.popBackStack() })
                }
                composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
                    val id = requireNotNull(entry.arguments).getInt("id")
                    val vm: ReminderDetailViewModel = viewModel(viewModelStoreOwner = entry,
                        factory = ReminderDetailViewModel.factory(repo, id))
                    ReminderDetailScreen(vm, onVolver = { nav.popBackStack() }, onEditar = { nav.navigate("edit/$it") })
                }
                composable("edit/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
                    val id = requireNotNull(entry.arguments).getInt("id")
                    val vm: EditReminderViewModel = viewModel(viewModelStoreOwner = entry,
                        factory = EditReminderViewModel.factory(repo, id))
                    EditReminderScreen(vm, onDone = { nav.popBackStack() }, onCancel = { nav.popBackStack() })
                }
            }
        }
    }
}
