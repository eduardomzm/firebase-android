package com.eduardomzm.android_firebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.eduardomzm.android_firebase.navigation.Navigation
import com.eduardomzm.android_firebase.ui.theme.AndroidFirebaseTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

class MainActivity : ComponentActivity() {
    private lateinit var analytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        analytics = Firebase.analytics

        setContent {
            AndroidFirebaseTheme {
                Navigation(analytics)
            }
        }
    }
}
