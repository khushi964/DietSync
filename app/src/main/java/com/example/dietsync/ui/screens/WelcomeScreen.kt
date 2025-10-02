package com.example.dietsync.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dietsync.R
import androidx.compose.ui.unit.sp as sp1

@Composable
fun WelcomeScreen(onNavigate: (String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(200.dp).padding(bottom = 16.dp)
            )

            Text(
                text = "Welcome to DietSync",
                fontSize = 35.sp1,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = { onNavigate("login") },
                modifier = Modifier.fillMaxWidth(0.4f).padding(vertical = 6.dp)
            ) {
                Text(
                    text = "Login",
                    fontSize = 20.sp1,
                    fontWeight = FontWeight.Medium

                )
            }

            Button(
                onClick = { onNavigate("signup") },
                modifier = Modifier.fillMaxWidth(0.4f).padding(vertical = 10.dp)
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 20.sp1,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Eat on time, live long !",
                fontSize = 30.sp1,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}
