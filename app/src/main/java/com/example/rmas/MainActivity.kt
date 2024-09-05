package com.example.rmas


import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

import com.example.rmas.ui.theme.RmasTheme



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()
        setContent {
            RmasTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(context = LocalContext.current)
                    MyAppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

}
@Composable
fun MainScreen(context: Context) {
    Column(modifier = Modifier.padding(16.dp)) {
        Button(onClick = { startLocationService(context) }) {
            androidx.compose.material3.Text(text = "Start Location Service")
        }
        Button(onClick = { stopLocationService(context) }) {
            androidx.compose.material3.Text(text = "Stop Location  Service")
        }
    }
}

private fun startLocationService(context: Context) {
    val intent = Intent(context, LocationService::class.java)
    ContextCompat.startForegroundService(context, intent)
}

private fun stopLocationService(context: Context) {
    val intent = Intent(context, LocationService::class.java)
    context.stopService(intent)
}