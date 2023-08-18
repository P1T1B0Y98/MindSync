package com.example.masterapp.presentation.screen.login

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.masterapp.R
import com.example.masterapp.presentation.screen.SharedViewModel
import com.example.masterapp.presentation.theme.Lavender
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(viewModel: LoginViewModel, onLoginSuccess: () -> Unit, onRegister: () -> Unit) {
    val emailState = remember { mutableStateOf("petter@petter.no") }
    val passwordState = remember { mutableStateOf("wilshere") }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 64.dp), // Ad
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            painter = painterResource(id = R.drawable.stress_management),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
        // Add the App Name and Slogan
        Text(
            text = "HealthPulse",
            style = MaterialTheme.typography.h4,
            color = Color(0xFF86C6E5),
            modifier = Modifier.padding(bottom = 5.dp)
        )
        Text(
            text = "Empowering Minds, Healing Hearts",
            style = MaterialTheme.typography.subtitle1,
            color = Color(0xFF86C6E5),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TextField(
            value = emailState.value,
            onValueChange = { emailState.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = passwordState.value,
            onValueChange = { passwordState.value = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val email = emailState.value
                val password = passwordState.value
                Log.i("LoginScreen", "email: $email, password: $password")
                coroutineScope.launch {
                    // Call the suspend function in the ViewModel using coroutines
                    viewModel.login(email, password) { success ->
                        if (success) {
                            onLoginSuccess()
                        }
                        else {
                            print("Login failed")
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
                .size(width = 200.dp, height = 50.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.baseline_login_24),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Login")
        }

        ClickableText(
            text = AnnotatedString("First time? Signup here"),
            style = TextStyle(
                color = Lavender, // Customize the link text color here
                textDecoration = TextDecoration.Underline,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold // Optional, you can adjust the font weight
            ),
            onClick = {
                // Navigate to the registration screen
                onRegister()
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 8.dp)
        )
    }

}
