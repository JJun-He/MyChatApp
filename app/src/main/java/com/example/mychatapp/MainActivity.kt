package com.example.mychatapp

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.net.URL


class MainActivity: ComponentActivity(){

    private lateinit var authRepository: AuthRepository
    private lateinit var chatRepository: ChatRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Firebase 초기화
        FirebaseApp.initializeApp(this)

        // 개발 환경에서 로컬 애뮬레이터 사용
        if(BuildConfig.DEBUG){
            val firestore = FirebaseFirestore.getInstance()
            val auth = FirebaseAuth.getInstance()

            // 애뮬레이터 연결 (애뮬레이터용 IP)
            firestore.useEmulator("10.0.2.2", 8080)
            auth.useEmulator("10.0.2.2", 9099)

        }

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

    private fun debugNetworkConnection() {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        Log.d("NetworkDebug", "네트워크 연결됨: ${networkInfo?.isConnected}")
        Log.d("NetworkDebug", "네트워크 타입: ${networkInfo?.typeName}")

        // 간단한 연결 테스트
        Thread {
            try {
                val connection = URL("https://www.google.com").openConnection()
                connection.connectTimeout = 5000
                connection.connect()
                Log.d("NetworkDebug", "✅ 구글 연결 성공")

                val firestoreConnection = URL("https://firestore.googleapis.com").openConnection()
                firestoreConnection.connectTimeout = 5000
                firestoreConnection.connect()
                Log.d("NetworkDebug", "✅ Firestore 연결 성공")
            } catch (e: Exception) {
                Log.e("NetworkDebug", "❌ 연결 실패: ${e.message}")
            }
        }.start()
    }
}