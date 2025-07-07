package com.example.mychatapp.data.repository

import android.util.Log
import com.example.mychatapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.collections.mapOf

/**
 * 사용자 인증 관련 데이터 접근을 담당하는 Repository 클래스
 * Firebase Authentication과 Firestore를 사용하여 사용자 관리
 */
class AuthRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance();
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * 현재 로그인된 사용자 정보를 가져오는 함수
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser


    /**
     * 현재 사용자의 UID를 가져오는 프로퍼티
     */
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    /**
     * Firestore에서 사용자 프로필 정보를 가져오는 함수
     */
    suspend fun getUserProfile(userId: String): Result<User>{
        return try{
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            if(userDoc.exists()){
                val user = userDoc.toObject(User::class.java)
                if(user != null){
                    Result.success(user)
                }else{
                    Result.failure(Exception("사용자 정보를 파싱할 수 없습니다"))
                }
            }else{
                // 문서가 없으면 FirebaseUser 정보로 새로 생성
                val firebaseUser = auth.currentUser
                if(firebaseUser != null){
                    val user = createUserFromFirebaseUser(firebaseUser)
                    Result.success(user)
                }else{
                    Result.failure(Exception("사용자 정보를 찾을 수 없습니다."))
                }
            }
        }catch (e: Exception){
            Log.e("AuthRepository", "Get user profile error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 이메일과 비밀번호로 회원가입하는 함수
     */
    suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String
    ): Result<User>{
        return try{
            // Firebase Authentication으로 사용자 생성
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("사용자 생성에 실패했습니다.")

            // Firebase에 사용자 정보 저장
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                displayName = displayName,
                photoUrl = firebaseUser.photoUrl?.toString()?: "",
                isOnline = true
            )

            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user.toMap())
                .await()

            Log.d("AuthRepository", "User signed up successfully:${user.uid}")
            Result.success(user)
        }catch (e: Exception){
            Log.e("AuthRepository", "Sign up error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 이메일과 비밀번호로 로그인하는 함수
     */
    suspend fun signInWithEmail(
        email: String,
        password: String
    ): Result<User>{
        return try {
            // Firebase Authentication으로 로그인
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("로그인에 실패했습니다")

            // Firestore에서 사용자 정보 가져오기
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = if(userDoc.exists()){
                userDoc.toObject(User::class.java) ?: createUserFromFirebaseUser(firebaseUser)
            }else{
                // Firestore에 사용자 정보가 없는 경우 새로 생성
                createUserFromFirebaseUser(firebaseUser)
            }

            // 사용자 온라인 상태 업데이트
            updateUserOnlineStatus(firebaseUser.uid, true)

            Log.d("AuthRepository", "User signed in successfully: ${user.uid}")
            Result.success(user.copy(isOnline = true))
        }catch (e: Exception){
            Log.e("AuthRepsoitory", "Sign in error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 로그아웃 함수
     */
    suspend fun signOut(): Result<Unit>{
        return try {
            currentUserId?.let{userId ->
                updateUserOnlineStatus(userId, false)
            }
            auth.signOut()
            Log.d("AuthRepository", "User signed out successfully")
            Result.success(Unit)
        }catch (e: Exception){
            Log.e("AuthRepository", "Sign out error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * FirebaseUser로부터 User 객체를 생성하는 함수
     */
    private suspend fun createUserFromFirebaseUser(firebaseUser: FirebaseUser): User{
        val user = User(
            uid = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName ?: "사용자",
            photoUrl = firebaseUser.photoUrl?.toString() ?: "",
            isOnline = true
        )

        // Firestore에 사용자 정보 저장
        firestore.collection("users")
            .document(firebaseUser.uid)
            .set(user.toMap())
            .await()

        return user;
    }

    /**
     * 사용자 온라인 상태를 업데이트하는 함수
     */
    private suspend fun updateUserOnlineStatus(userId: String, isOnline: Boolean){
        try{
            val updates = mapOf(
                "isOnline" to isOnline,
                "lastSeen" to if(isOnline) null else com.google.firebase.Timestamp.now()
            )

            firestore.collection("users")
                .document(userId)
                .update(updates)
                .await()
        }catch (e: Exception){
            Log.e("AuthRepository", "Failed to update online status: ${e.message}")
        }
    }
}