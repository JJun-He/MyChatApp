package com.example.mychatapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


/**
 * 채팅방 정보를 나타내는 데이터 클래스
 */
data class ChatRoom(
    val uid: String = "",
    val name: String = "",
    val participants: List<String> = listOf(),
    val lastMessage: String = "",
    @ServerTimestamp
    val lastMessageTime: Timestamp? = null,
    val createdBy: String = "",
    val isGroup: Boolean = false
)