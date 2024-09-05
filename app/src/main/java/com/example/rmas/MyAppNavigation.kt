
package com.example.rmas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.example.rmas.pages.LoginPage
import com.example.rmas.pages.SignupPage
import com.example.rmas.pages.MapPage

import com.example.rmas.pages.FilterPage


@Composable
fun MyAppNavigation(modifier: Modifier = Modifier,authViewModel: AuthViewModel) {


    val navController = rememberNavController()


    NavHost(navController = navController, startDestination = "login", builder = {
        composable("login"){
            LoginPage(modifier,navController,authViewModel)
        }
        composable("signup"){
            SignupPage(modifier,navController,authViewModel)
        }
        composable("home"){
            HomePage(modifier,navController,authViewModel)
        }
        composable("map") {
            MapPage(
                navController = navController,
                modifier = Modifier

            )
        }
        composable("filterPage") { FilterPage(navController) }



    })
}