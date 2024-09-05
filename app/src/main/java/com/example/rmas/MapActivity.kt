

package com.example.rmas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold

import androidx.compose.ui.Modifier
import com.example.rmas.ui.theme.RmasTheme
import com.example.rmas.pages.MapPage
import androidx.navigation.NavController
import android.util.Log
import androidx.navigation.compose.rememberNavController

class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MapActivity", "MapActivity started")
        setContent {
            RmasTheme {
                val navController = rememberNavController()
                Scaffold { innerPadding ->
                    Log.d("MapActivity", "MapPage is being set")
                    MapPage(navController = navController,
                        modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding))
                }
            }
        }
    }
}