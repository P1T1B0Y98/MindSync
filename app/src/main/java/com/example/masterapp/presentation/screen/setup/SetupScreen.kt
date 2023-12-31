package com.example.masterapp.presentation.screen.setup

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import com.example.masterapp.NotificationSettingsManager
import com.example.masterapp.R
import com.example.masterapp.presentation.BaseApplication
import com.example.masterapp.presentation.navigation.Screen
import com.example.masterapp.presentation.theme.Lavender

import java.util.UUID

@Composable
fun SetupScreen(
    viewModel: SetupScreenViewModel,
    navController: NavController,
    uiState: SetupScreenViewModel.UiState,
    onPermissionsLaunch: (Set<String>) -> Unit = {},
    notificationSettingsManager: NotificationSettingsManager

    ) {

    val context = LocalContext.current
    val app = context.applicationContext as BaseApplication
    val preferencesHelper = app.preferencesHelper

    val errorId = rememberSaveable { mutableStateOf(UUID.randomUUID()) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(uiState) {
        if (uiState is SetupScreenViewModel.UiState.Error && errorId.value != uiState.uuid) {
            errorId.value = uiState.uuid
        }
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshUI()
            }
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    Log.i("SetupScreen", "uiState: $uiState")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)

    ) {
        when (uiState) {
            is SetupScreenViewModel.UiState.AppNotInstalled -> {
                // Render the permissions content if the app is not installed
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Get started",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )

                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground_dup),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )

                    StepIndicator(currentStep = 1, totalSteps = 2)

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                        elevation = 8.dp,
                        backgroundColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth().height(250.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.health_connect_installation_description),
                                fontSize = 16.sp,
                                color = MaterialTheme.colors.primary,
                                textAlign = TextAlign.Start,
                            )
                            Spacer(modifier = Modifier.height(50.dp))
                            Button(
                                onClick = { viewModel.openAppPage() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.setup_health_connect_button_label),
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            is SetupScreenViewModel.UiState.PermissionsNotGranted -> {
                // Render the permissions content if permissions are not granted
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Get started",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colors.primary
                    )

                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground_dup),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )

                    StepIndicator(currentStep = 2, totalSteps = 2)
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                        elevation = 8.dp,
                        backgroundColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = stringResource(R.string.permissions_description),
                                fontSize = 18.sp,
                                color = MaterialTheme.colors.primary,
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    onPermissionsLaunch(viewModel.permissions)
                                    preferencesHelper.setSetupComplete(true)
                                          },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                            ) {
                                Text(
                                    text = stringResource(R.string.permissions_button_label),
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colors.primary),
                        elevation = 8.dp,
                        backgroundColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)  // Adjust the height as required
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val isNotificationEnabled = viewModel.isNotificationEnabled

                            Text(
                                text = if (isNotificationEnabled) {
                                    "You have notifications enabled. Remember to check them regularly."
                                } else {
                                    "Notifications are required to remind you to fill out your questionnaire. You can change your notification settings later in the app. Or right now if you click the button underneath."
                                },
                                fontSize = 18.sp,
                                color = MaterialTheme.colors.primary,
                                textAlign = TextAlign.Start
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.requestNotificationPermission() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                            ) {
                                Text(
                                    text = "Notification Settings",
                                    fontSize = 16.sp,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    ClickableText(
                        text = AnnotatedString("Skip and fix it later"),
                        style = TextStyle(
                            color = Lavender, // Customize the link text color here
                            textDecoration = TextDecoration.Underline,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold // Optional, you can adjust the font weight
                        ),
                        onClick = {
                            // Navigate to the registration screen
                            navController.navigate(Screen.HomeScreen.route)
                            preferencesHelper.setSetupComplete(true)
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 8.dp)
                    )
                }
            }

            else -> {}
        }
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Text(
        text = "Step $currentStep of $totalSteps",
        fontSize = 20.sp,
        color = MaterialTheme.colors.primary
    )
}
