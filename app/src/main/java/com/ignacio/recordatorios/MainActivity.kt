package com.ignacio.recordatorios

import android.os.Bundle
import android.net.ConnectivityManager
import android.net.Network
import android.content.Context
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ignacio.recordatorios.data.RepositoryProvider
import com.ignacio.recordatorios.ui.*
import com.ignacio.recordatorios.ui.theme.RecordatoriosTheme

class MainActivity : ComponentActivity() {
    private var manager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Optional automatic retry when the device reconnects. Room remains authoritative.
        manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                lifecycleScope.launch { RepositoryProvider.repository(applicationContext).sync() }
            }
        }
        networkCallback = callback
        manager?.registerDefaultNetworkCallback(callback)
        setContent { ReminderApp() }
    }
    override fun onDestroy() {
        networkCallback?.let { manager?.unregisterNetworkCallback(it) }
        super.onDestroy()
    }
    @Composable private fun ReminderApp() {
        val nav = rememberNavController()
        val repo = remember { RepositoryProvider.repository(applicationContext) }
        val prefs = remember { RepositoryProvider.preferences(applicationContext) }
        val settingsVm: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(prefs))
        val options by settingsVm.state.collectAsStateWithLifecycle()
        val listVm: ReminderListViewModel = viewModel(factory = ReminderListViewModel.factory(repo, prefs))
        RecordatoriosTheme(darkTheme = options.darkMode) {
            NavHost(navController = nav, startDestination = "list") {
                composable("list") {
                    ReminderScreen(listVm, onCrearRecordatorio = { nav.navigate("create") },
                        onRecordatorioClick = { nav.navigate("detail/$it") }, onSettings = { nav.navigate("settings") })
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
