package com.example.mychatapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


/**
 * 메시지 타입을 정의하는 enum class
 */

enum class MessageType{
    TEXT, IMAGE, SYSTEM
}

/**
 * 채팅 메시지를 나타내는 데이터 클래스
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String= "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val type: MessageType = MessageType.TEXT,
    val chatRoomId: String = "",
    val imageUrl: String = ""
)
