package com.eduardomzm.android_firebase.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.eduardomzm.android_firebase.navigation.TrackScreen
import com.google.firebase.analytics.FirebaseAnalytics

@Composable
fun Home(analytics: FirebaseAnalytics) {
    TrackScreen(name = "HomeScreen", analytics = analytics)

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Inicio", fontSize = 40.sp)
    }
}
