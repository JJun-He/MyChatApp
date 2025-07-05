package com.example.mychatapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


/*
* 사용자 정보를 나타내는 데이터 클래스
* Firebase Authentication과 FireStore에 저장되는 사용자 데이터
* */

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String= "",
    val isOnline: Boolean = false,
    @ServerTimestamp
    val lastSeen: Timestamp? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null
){
    /**
     * Firestore에 저장하기 위한 Map 변환 함수
     */
    fun toMap(): Map<String, Any?>{
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "isOnline" to isOnline,
            "lastSeen" to lastSeen,
            "createdAt" to createdAt
        )
    }
}