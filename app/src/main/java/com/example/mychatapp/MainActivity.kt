package com.example.mychatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.mychatapp.data.repository.AuthRepository
import com.example.mychatapp.data.repository.ChatRepository
import com.example.mychatapp.presentation.navigation.AppNavigation
import com.example.mychatapp.ui.theme.MyChatAppTheme
import com.google.firebase.FirebaseApp


class MainActivity: ComponentActivity(){

    private lateinit var authRepository: AuthRepository
    private lateinit var chatRepository: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        // Respository 초기화
        authRepository = AuthRepository()
        chatRepository = ChatRepository()

        setContent {
            MyChatAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {innerPadding ->
                    AppNavigation(
                        authRepository = authRepository,
                        chatRepository = chatRepository,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}