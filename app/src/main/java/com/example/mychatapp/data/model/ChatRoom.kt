package com.example.mychatapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp


/**
 * 채팅방 정보를 나타내는 데이터 클래스
 */
data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val participants: List<String> = listOf(),
    val particpantNames: Map<String, String> = mapOf(), // userId -> displayName
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageSenderName: String = "",
    @ServerTimestamp
    val lastMessageTime: Timestamp? = null,
    val createdBy: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val isGroup: Boolean = false
){
    /**
     * 개인 채팅방에서 상대방 이름 가져오기
     */
    fun getOtherParticipantName(currentUserId: String): String{
        return if(!isGroup && participants.size == 2){
            val otherUserId = participants.find{it != currentUserId}
            participantNames[otherUserId] ?: "알 수 없는 사용자"
        }else{
            name
        }
    }

    /**
     * 채팅방 표시 이름 가져오기
     */
    fun getDisplayName(currentUserId: String): String{
        return if(isGroup){
            name
        }else{
            getOtherParticipantName(currentUserId)
        }
    }
}