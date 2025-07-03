package com.example.mychatapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mychatapp.ui.theme.MyChatAppTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Firebase 초기화 - 명시적으로 호출하여 확실히 초기화
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setContent {
            MyChatAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FirebaseConnectionTest(
                        auth = auth,
                        firestore = firestore,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

/**
 * Firebase 연결 상태를 테스트하고 결과를 UI에 표시하는 Composable 함수
 *
 * @param auth FirebaseAuth 인스턴스
 * @param firestore FirebaseFirestore 인스턴스
 * @param modifier UI 수정자
 */
@Composable
fun FirebaseConnectionTest(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    modifier: Modifier = Modifier
) {
    // 연결 상태 메시지를 저장하는 상태 변수
    var connectionStatus by remember { mutableStateOf("Firebase 연결 테스트 중...") }
    // 로딩 상태를 관리하는 상태 변수
    var isLoading by remember { mutableStateOf(true) }

    // LaunchedEffect를 사용하여 Composable이 처음 구성될 때 한 번만 연결 테스트 실행
    // Unit 키를 사용하면 Composable이 재구성되어도 다시 실행되지 않음
    LaunchedEffect(Unit) {
        testFirebaseConnection(
            auth = auth,
            firestore = firestore,
            onResult = { status ->
                connectionStatus = status
                isLoading = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 로딩 중일 때 CircularProgressIndicator 표시
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 연결 상태에 따라 다른 색상으로 텍스트 표시
        Text(
            text = connectionStatus,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = when {
                connectionStatus.contains("성공") -> MaterialTheme.colorScheme.primary
                connectionStatus.contains("실패") -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )

        // 성공 시 추가 정보 카드 표시
        if (connectionStatus.contains("성공")) {
            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 준비 완료!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "채팅앱 개발을 시작할 수 있습니다",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/**
 * Firebase 연결을 테스트하는 함수
 *
 * @param auth FirebaseAuth 인스턴스
 * @param firestore FirebaseFirestore 인스턴스
 * @param onResult 결과를 받을 콜백 함수
 */
private fun testFirebaseConnection(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    onResult: (String) -> Unit
) {
    // Authentication 연결 테스트 - 로그로 확인
    Log.d("Firebase", "Auth instance: ${auth.app.name}")

    // Firestore 연결 테스트 - 실제 데이터 쓰기 시도
    val testData = mapOf(
        "message" to "Firebase 연결 테스트 (Compose)",
        "timestamp" to System.currentTimeMillis(),
        "platform" to "Android Compose"
    )

    firestore.collection("test")
        .document("connection")
        .set(testData)
        .addOnSuccessListener {
            Log.d("Firebase", "Firestore 연결 성공!")
            onResult("Firebase 연결 성공!\n채팅앱 개발 준비 완료")
        }
        .addOnFailureListener { exception ->
            Log.e("Firebase", "Firestore 연결 실패: ${exception.message}")
            onResult("Firebase 연결 실패\n설정을 다시 확인해주세요\n\n오류: ${exception.message}")
        }
}
