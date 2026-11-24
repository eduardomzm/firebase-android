package com.eduardomzm.android_firebase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.eduardomzm.android_firebase.navigation.Navigation
import com.eduardomzm.android_firebase.ui.theme.AndroidFirebaseTheme
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidFirebaseTheme() {
                Navigation()
            }
        }
    }
}