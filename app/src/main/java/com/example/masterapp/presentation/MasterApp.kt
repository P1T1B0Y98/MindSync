package com.example.masterapp.presentation

import AuthManager
import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.masterapp.presentation.theme.Lavender
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.masterapp.R
import com.example.masterapp.data.HealthConnectManager
import com.example.masterapp.presentation.navigation.AppNavigator
import com.example.masterapp.presentation.navigation.Drawer
import com.example.masterapp.presentation.theme.MasterAppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch


@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun MasterApp(
    healthConnectManager: HealthConnectManager,
    authManager: AuthManager) {
    MasterAppTheme {
        val systemUiController = rememberSystemUiController()
        val scaffoldState = rememberScaffoldState()
        val navController = rememberNavController()
        val scope = rememberCoroutineScope()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val availability by healthConnectManager.availability

        // Get the user's login status from authManager
        val userLoggedIn = authManager.isSignedIn()
        Log.i("MasterApp", "userLoggedIn: $userLoggedIn")


        SideEffect {
            systemUiController.setStatusBarColor(
                color = Lavender,
                darkIcons = false
            )
        }

        Scaffold(
            scaffoldState = scaffoldState,

            topBar = {
                TopAppBar(
                    title = {
                        val title = when (currentRoute) {
                            else -> stringResource(id = R.string.app_name)
                        }
                        Text(text = title,
                            textAlign = TextAlign.Left,
                            modifier = Modifier.padding(start = 50.dp))
                    },

                    navigationIcon = {
                        if (userLoggedIn && availability == SDK_AVAILABLE) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        scaffoldState.drawerState.open()
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu,
                                    stringResource(id = R.string.menu)
                                )
                            }
                        }
                    }
                )
            },
            drawerContent = {
                if (availability == SDK_AVAILABLE) {
                    Drawer(
                        scope = scope,
                        scaffoldState = scaffoldState,
                        navController = navController,
                        authManager = authManager
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(it) { data -> Snackbar(snackbarData = data) }
            }
        ) {
            AppNavigator(
                healthConnectManager = healthConnectManager,
                navController = navController,
                scaffoldState = scaffoldState,
                authManager = authManager
            )
        }
    }
}