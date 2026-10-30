package com.ignacio.recordatorios

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ignacio.recordatorios.data.RepositoryProvider
import com.ignacio.recordatorios.ui.*
import com.ignacio.recordatorios.ui.theme.RecordatoriosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { RecordatoriosTheme { ReminderApp() } }
    }
}

@Composable
private fun ReminderApp() {
    val navController = rememberNavController()
    val repository = RepositoryProvider.repository
    // Instancia compartida al nivel del grafo: el listado se refresca al regresar.
    val listVm: ReminderListViewModel = viewModel(factory = ReminderListViewModel.factory(repository))
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            ReminderScreen(viewModel = listVm,
                onCrearRecordatorio = { navController.navigate("create") },
                onRecordatorioClick = { id -> navController.navigate("detail/$id") })
        }
        composable("create") { entry ->
            val vm: CreateReminderViewModel = viewModel(viewModelStoreOwner = entry,
                factory = CreateReminderViewModel.factory(repository))
            CreateReminderScreen(viewModel = vm,
                onGuardar = { listVm.load(); navController.popBackStack() },
                onCancelar = { navController.popBackStack() })
        }
        composable("detail/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) { entry ->
            val id = requireNotNull(entry.arguments).getInt("id")
            val vm: ReminderDetailViewModel = viewModel(viewModelStoreOwner = entry,
                factory = ReminderDetailViewModel.factory(repository, id))
            ReminderDetailScreen(vm, onVolver = { navController.popBackStack() })
        }
    }
}
