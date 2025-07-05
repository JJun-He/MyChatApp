package com.example.mychatapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.mychatapp.data.repository.AuthRepository


// ViewModel 생성을 위한 Factory
/**
 * AuthViewModel을 생성하기 위한 팩토리 클래스
 */
class AuthViewModelFactory (
    private val authRepository: AuthRepository
): ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T: ViewModel> create(modelClass: Class<T>): T{
        if(modelClass.isAssignableFrom(AuthViewModel::class.java)){
            return AuthViewModel(authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}