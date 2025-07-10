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
){
    /**
     * 시간 표시용 포맷된 시간 문자열 반환
     */
    fun getFormattedTime(): String{
        return timestamp?.toDate().let{date ->
            val now = System.currentTimeMillis()
            val messageTime = date?.time ?: 0L
            val diffInHours = (now - messageTime) / (1000 * 60 * 60)

            when{
                diffInHours < 24 ->{
                    java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
                }
                diffInHours < 24*7 ->{
                    java.text.SimpleDateFormat("E HH:mm", java.util.Locale.getDefault()).format(date)
                }

                else -> {
                    java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault()).format(date)
                }
            }
        }?:""
    }
}
