package com.eduardomzm.android_firebase.ui.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.eduardomzm.android_firebase.ui.screens.HomeScreen
import com.eduardomzm.android_firebase.ui.screens.Screen
import com.eduardomzm.android_firebase.ui.screens.auth.ForgotPasswordScreen
import com.eduardomzm.android_firebase.ui.screens.auth.LoginScreen
import com.eduardomzm.android_firebase.ui.screens.auth.SignUpScreen
import com.eduardomzm.android_firebase.utils.AnalyticsManager
import com.eduardomzm.android_firebase.utils.AuthManager

@Composable
fun Navigation(context: Context, navController: NavHostController = rememberNavController()) {
    val analytics = remember { AnalyticsManager(context) }
    val authManager = remember { AuthManager() }

    // Si ya hay una sesión abierta, la app arranca directamente en Home
    val startDestination = remember {
        if (authManager.getCurrentUser() == null) Routes.Login.route else Routes.Home.route
    }

    Screen {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Routes.Login.route) {
                LoginScreen(
                    analytics = analytics,
                    auth = authManager,
                    navigation = navController,
                )
            }
            composable(Routes.Home.route) {
                HomeScreen(
                    analytics = analytics,
                    auth = authManager,
                    navigation = navController
                )
            }
            composable(Routes.SignUp.route) {
                SignUpScreen(
                    analytics = analytics,
                    auth = authManager,
                    navigation = navController
                )
            }
            composable(Routes.ForgotPassword.route) {
                ForgotPasswordScreen(
                    analytics = analytics,
                    auth = authManager,
                    navigation = navController
                )
            }
        }
    }
}
