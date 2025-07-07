package com.example.mychatapp.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mychatapp.data.model.AuthState
import com.example.mychatapp.presentation.viewmodel.AuthViewModel


/**
 * 사용자 인증 화면 (로그인/회원가입 통합)
 */
@Composable
fun AuthScreen (
    authViewModel: AuthViewModel,
    onAuthSuccess: () -> Unit
){
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 인증 성공 시 처리
    LaunchedEffect(authState) {
        when(authState) {
            is AuthState.Authenticated -> {
                onAuthSuccess()
            }
            else -> {}
        }
    }

    // 에러 메시지 표시
    LaunchedEffect(errorMessage) {
        errorMessage?.let{
            message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            authViewModel.clearErrorMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 제목
        Text(
            text = if(isLoginMode) "로그인" else "회원가입",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 회원가입 모드일 때만 이름 입력 필드 표시
        if (!isLoginMode){
            OutlinedTextField(
                value = displayName,
                onValueChange = {displayName = it},
                label = {Text("이름")},
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = "이름")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 이메일 입려 필드
        OutlinedTextField(
            value = email,
            onValueChange = {email = it},
            label = {Text("이메일")},
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = "이메일")
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 비밀번호 입력 필드
        OutlinedTextField(
            value = password,
            onValueChange = {password = it},
            label = {Text("비밀번호")},
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = "비밀번호")
            },
            trailingIcon = {
                IconButton(onClick = {passwordVisible = !passwordVisible}) {
                    Icon(
                        imageVector = if(passwordVisible) Icons.Default.Visibility else
                        Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보이기"
                    )
                }
            },
            visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 로그인/회원가입 버튼
        Button(
            onClick = {
                if(isLoginMode){
                    authViewModel.signIn(email, password)
                }else{
                    authViewModel.signUp(email, password, displayName)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading
        ) {
            if(isLoading){
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }else{
                Text(
                    text = if(isLoginMode) "로그인" else "회원가입",
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 모드 전환 버튼
        TextButton(
            onClick = {
                isLoginMode = !isLoginMode
                // 모드 전환 시 입력 필드 초기화
                email = ""
                password = ""
                displayName = ""
            }
        ) {
            Text(
                text = if(isLoginMode) "계정이 없으신가요? 회원가입" else "이미 계정이 있으신가요? 로그인",
                textAlign = TextAlign.Center
            )
        }
    }
}