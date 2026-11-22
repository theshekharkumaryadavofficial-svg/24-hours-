package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.NaamJapRepository
import com.example.data.PlansRepository
import com.example.data.SleepDatabase
import com.example.data.SleepRepository
import com.example.ui.MainAppScreen
import com.example.ui.NaamJapViewModel
import com.example.ui.NaamShaktiViewModel
import com.example.ui.PlansViewModel
import com.example.ui.SleepViewModel
import com.example.ui.theme.TwentyFourHrsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        val database = SleepDatabase.getInstance(applicationContext)
        val sleepRepository = SleepRepository(database.sleepDao())
        val naamJapRepository = NaamJapRepository(database.naamJapDao())
        val plansRepository = PlansRepository(database.plansDao())

        val sleepFactory = SleepViewModel.Factory(applicationContext, sleepRepository)
        val naamJapFactory = NaamJapViewModel.Factory(naamJapRepository)
        val plansFactory = PlansViewModel.Factory(applicationContext, plansRepository, naamJapRepository)
        val naamShaktiFactory = NaamShaktiViewModel.Factory(
            context = applicationContext,
            naamJapRepository = naamJapRepository,
            naamShaktiDao = database.naamShaktiDao()
        )

        setContent {
            TwentyFourHrsTheme {
                val sleepViewModel: SleepViewModel = viewModel(factory = sleepFactory)
                val naamJapViewModel: NaamJapViewModel = viewModel(factory = naamJapFactory)
                val plansViewModel: PlansViewModel = viewModel(factory = plansFactory)
                val naamShaktiViewModel: NaamShaktiViewModel = viewModel(factory = naamShaktiFactory)
                MainAppScreen(
                    sleepViewModel = sleepViewModel,
                    naamJapViewModel = naamJapViewModel,
                    plansViewModel = plansViewModel,
                    naamShaktiViewModel = naamShaktiViewModel
                )
            }
        }
    }
}
