package com.example.mychatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mychatapp.data.model.AuthState
import com.example.mychatapp.data.repository.AuthRepository
import com.example.mychatapp.presentation.screen.AuthScreen
import com.example.mychatapp.presentation.screen.MainScreen
import com.example.mychatapp.presentation.viewmodel.AuthViewModel
import com.example.mychatapp.presentation.viewmodel.AuthViewModelFactory
import com.example.mychatapp.ui.theme.MyChatAppTheme
import com.google.firebase.FirebaseApp



class MainActivity: ComponentActivity(){

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        // AuthRepository 초기화
        authRepository = AuthRepository()

        setContent {
            MyChatAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {
                    innerPadding ->
                    // AuthViewModel 생성
                    val authViewModel : AuthViewModel = viewModel(
                        factory = AuthViewModelFactory(authRepository)
                    )

                    // 인증 상태 관찰
                    val authState by authViewModel.authState.collectAsStateWithLifecycle()

                    val currentAuthState = authState
                    // 인증 상태에 따라 화면 전환
                    when(currentAuthState){
                        is AuthState.Loading -> {
                            // 로딩 화면(간단한 화면)
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ){
                                androidx.compose.material3.CircularProgressIndicator()
                            }
                        }

                        is AuthState.Unauthenticated -> {
                            AuthScreen(
                                authViewModel = authViewModel,
                                onAuthSuccess = {
                                    // 인증 성공 시 자동으로 AuthState.Authenticated로 변경됨
                                }
                            )
                        }

                        is AuthState.Authenticated -> {
                            MainScreen(
                                authViewModel = authViewModel,
                                onSignOut = {
                                    // 로그아웃 시 자동으로 AuthState.UnAuthenticated로 변경됨
                                }
                            )
                        }

                        is AuthState.Error -> {
                            // 에러 화면 (간단한 구현)
                            androidx.compose.foundation.layout.Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                            ) {
                                androidx.compose.material3.Text(
                                    text = "오류가 발생했습니다: ${currentAuthState.message}",
                                    color = androidx.compose.material3.MaterialTheme.colorScheme.error
                                )
                                androidx.compose.foundation.layout.Spacer(
                                    modifier = Modifier.height(16.dp)
                                )
                                androidx.compose.material3.Button(
                                    onClick = {authViewModel.checkAuthState()}
                                ) {
                                    androidx.compose.material3.Text("다시 시도")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}