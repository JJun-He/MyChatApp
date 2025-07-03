package com.example.mychatapp.data.model


/**
 * 인증 작업의 결과를 나타내는 sealed class
 * 성공, 실패, 로딩 상태를 구분하여 처리
 */
sealed class AuthResult<out T> {
    object Loading: AuthResult<Nothing>()
    data class Success<T>(val data: T): AuthResult<T>()
    data class Error(val exception: Exception): AuthResult<Nothing>()
}

/**
 * 사용자 인증 상태를 나타내는 sealed class
 */
sealed class AuthState{
    object Loading: AuthState()
    object Unauthenticated: AuthState()
    data class Authenticated(val user: User): AuthState()
    data class Error(val message: String): AuthState()
}