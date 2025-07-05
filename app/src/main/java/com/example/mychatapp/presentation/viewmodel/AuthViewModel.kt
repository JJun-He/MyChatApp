package com.example.mychatapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mychatapp.data.model.AuthState
import com.example.mychatapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * 인증 관련 UI 상태를 관리하는 ViewModel
 */
class AuthViewModel(private val authRepository: AuthRepository): ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        checkAuthState()
    }

    /**
     * 현재 인증 상태를 확인하는 함수
     */
    private fun checkAuthState(){
        val currentUser = authRepository.currentUser
        _authState.value = if(currentUser != null){
            AuthState.Loading // 실제로는 Firestore에서 사용자 정보를 가져와야 함
        }else{
            AuthState.Unauthenticated
        }
    }

    /**
     *  이메일과 비밀번호로 회원가입하는 함수
     */
    fun signUp(email: String, password: String, displayName: String){
        if(!isValidInput(email, password, displayName)) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.signUpWithEmail(email, password, displayName)
            _isLoading.value = false

            result.onSuccess {
                user -> _authState.value = AuthState.Authenticated(user)
            }.onFailure {
                exception -> _errorMessage.value = getErrorMessage(exception)
            }
        }
    }

    /**
     * 이메일과 비밀번호를 로그인하는 함수
     */
    fun signIn(email: String, password: String){
        if(!isValidInput(email, password)) return

        viewModelScope.launch{
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.signInWithEmail(email, password)
            _isLoading.value = false

            result.onSuccess {
                user -> _authState.value = AuthState.Authenticated(user)
            }.onFailure {
                exception -> _errorMessage.value = getErrorMessage(exception)
            }
        }
    }

    /**
     * 로그아웃 함수
     */
    fun signOut(){
        viewModelScope.launch {
            authRepository.signOut()
            _authState.value = AuthState.Unauthenticated
        }
    }

    /**
     * 에러 메시지를 지우는 함수
     */
    fun clearErrorMessage(){
        _errorMessage.value = null
    }

    /**
     * 입력값 유효성 검사 함수
     */
    private fun isValidInput(email: String, password: String, displayName: String?= null): Boolean{
        return when{
            email.isBlank() -> {
                _errorMessage.value = "이메일을 입력해주세요"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _errorMessage.value = "올바른 이메일 형식을 입력해주세요"
                false
            }
            password.isBlank() -> {
                _errorMessage.value = "비밀번호를 입력해주세요"
                false
            }
            password.length < 6 -> {
                _errorMessage.value = "이름을 입력해주세요"
                false
            }
            else -> true
        }
    }

    /**
     * Firebase Exception을 사용자 친화적인 메시지로 변환하는 함수
     */
    private fun getErrorMessage(exception: Throwable): String{
        return when(exception.message){
            "The email address is already in use by another account." ->
                "이미 사용 중인 이메일 주소입니다."
            "The password is invalid or the user does not have a password." ->
                "비밀번호가 올바르지 않습니다."
            "There is no user record corresponding to this identifier. The user may have been deleted." ->
                "존재하지 않는 사용자입니다."
            "The email address is badly formatted." ->
                "이메일 형식이 올바르지 않습니다."
            else -> exception.message ?: "알 수 없는 오류가 발생했습니다."
        }
    }
}