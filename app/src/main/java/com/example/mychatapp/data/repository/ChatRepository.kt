package com.example.mychatapp.data.repository

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import com.example.mychatapp.data.model.ChatRoom
import com.example.mychatapp.data.model.Message
import com.example.mychatapp.data.model.MessageType
import com.example.mychatapp.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * 채팅 관련 데이터 접근을 담당하는 Repository 클래스
 */
class ChatRepository{
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * 현재 사용자가 참여 중인 채팅방 목록을 실시간으로 가져오는 함수
     */
    fun getChatRooms(): Flow<List<ChatRoom>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid
        if(currentUserId == null){
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("chatRooms")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    Log.e("ChatRepository", "Error getting chat rooms: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val chatRooms = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject<ChatRoom>()?.copy(id = doc.id)
                }?: emptyList()

                trySend(chatRooms)
            }

        awaitClose { listener.remove() }
    }

    /**
     * 특정 채팅방의 메시지를 실시간으로 가져오는 함수
     */
    fun getMessages(chatRoomId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("messages")
            .whereEqualTo("chatRoomId", chatRoomId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if(error != null){
                    Log.e("ChatRepository", "Error getting messages: ${error.message}")
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val messages = snapshot?.documents?.mapNotNull{doc ->
                    val message = doc.toObject<Message>()?.copy(id = doc.id)
                    // MessageType enum 변환
                    message?.copy(
                        type = try{
                            MessageType.valueOf(doc.getString("type") ?: "TEXT")
                        }catch (e: Exception){
                            MessageType.TEXT
                        }
                    )
                }?: emptyList()

                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    /**
     * 메시지를 전송하는 함수
     */
    suspend fun sendMessages(
        chatRoomId: String,
        content: String,
        type: MessageType = MessageType.TEXT
    ): Result<Unit>{
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("Chat Repository", "User not authenticated")
                return Result.failure(Exception("로그인이 필요합니다"))
            }

            Log.e(
                "ChatRepository",
                "Attempting to send message - ChatRoom: $chatRoomId, Content: $content"
            )

            val messageRef = firestore.collection("messages").document()

            // 메시지 객체를 Map으로 변환하여 전송(리플렉션 해결)
            var messageData = hashMapOf(
                "id" to messageRef.id,
                "senderid" to currentUser.uid,
                "senderName" to (currentUser.displayName ?: "사용자"),
                "content" to content,
                "type" to type.name, // enum을 문자열로 저장
                "chatRoomId" to chatRoomId,
                "timestamp" to com.google.firebase.Timestamp.now()
            )

            // 메시지 저장
            messageRef.set(messageData).await()
            Log.e("ChatRepository", "Message saved successfully: ${messageRef.id}")

            // Message 객체 생성 (마지막 메시지 업데이트용)
            val message = Message(
                id = messageRef.id,
                senderId = currentUser.uid,
                senderName = currentUser.displayName ?: "사용자",
                content = content,
                type = type,
                chatRoomId = chatRoomId,
                timestamp = com.google.firebase.Timestamp.now()
            )

            // 채팅방의 마지막 메시지 정보 업데이트
            updateChatRoomLastMessage(chatRoomId, message)

            Log.d("ChatRepository", "Message sent Successfully: ${message.id}")
            Result.success(Unit)
        } catch (e: Exception){
            Log.e("ChatRepository", "Error sending message: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 채팅방의 마지막 메시지 정보를 업데이트하는 함수
     */
    private suspend fun updateChatRoomLastMessage(chatRoomId: String, message: Message){
        try {
            val chatRoomRef = firestore.collection("chatRooms").document(chatRoomId)
            val updates = hashMapOf<String, Any>(
                "lastMessage" to when (message.type){
                    MessageType.TEXT -> message.content
                    MessageType.IMAGE -> "이미지"
                    MessageType.SYSTEM -> message.content
                },
                "lastMessageSenderId" to message.senderId,
                "lastMessageSenderName" to message.senderName,
                "lastMessageTime" to com.google.firebase.Timestamp.now()
            )

            chatRoomRef.update(updates).await()
            Log.d("Chat Repository", "Last message updated successfully")
        }catch (e: Exception){
            Log.e("ChatRepository", "Error updating last message: ${e.message}",e)
        }
    }

    /**
     * 새로운 채팅방을 생성하는 함수
     */
    suspend fun createChatRoom(
        name: String,
        participantIds : List<String>,
        isGroup: Boolean = true
    ): Result<ChatRoom>{
        return try {
            val currentUserId = auth.currentUser?.uid?:throw Exception("로그인이 필요합니다")
            val currentUserName = auth.currentUser?.displayName?: "사용자"

            // 참여자 정보 가져오기
            val participantNames = mutableMapOf<String, String>()
            val allParticipants = (participantIds + currentUserId).distinct()

            for(userId in allParticipants){
                val userDoc = firestore.collection("users").document(userId).get().await()
                val user = userDoc.toObject<User>()
                participantNames[userId] = user?.displayName?:"알 수 없는 사용자"
            }

            // 개인 채팅방인 경우 이미 존재하는지 확인
            if(!isGroup && allParticipants.size == 2){
                val existingChatRoom = findExistingPrivateChat(allParticipants)
                if(existingChatRoom != null){
                    return Result.success(existingChatRoom)
                }
            }

            // 새 채팅방 생성
            val chatRoomRef = firestore.collection("chatRooms").document()
            val chatRoom = ChatRoom(
                id = chatRoomRef.id,
                name = if(isGroup) name else "",
                participants = allParticipants,
                particpantNames = participantNames,
                createdBy = currentUserId,
                isGroup = isGroup,
                lastMessage = if (isGroup) "${currentUserName}님이 채팅방을 생성했습니다." else "새로운 채팅이 시작되었습니다."

            )

            chatRoomRef.set(chatRoom).await()

            Log.d("ChatRepository", "Chat room created: ${chatRoom.id}")
            Result.success(chatRoom)
        }catch (e: Exception){
            Log.e("ChatRepository", "Error creating chat room: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 기존 개인 채팅방을 찾는 함수
     */
    private suspend fun findExistingPrivateChat(participants: List<String>): ChatRoom? {
        return try {
            val snapshot = firestore.collection("chatRooms")
                .whereEqualTo("isGroup", false)
                .whereArrayContainsAny("participants", participants)
                .get()
                .await()

            snapshot.documents.firstOrNull{
                doc ->
                val chatRoom = doc.toObject<ChatRoom>()
                chatRoom?.participants?.toSet() == participants.toSet()
            }?.toObject<ChatRoom>()?.copy(id = snapshot.documents.first().id)
        }catch (e: Exception){
            Log.e("ChatRepository", "Error finding existing private chat: ${e.message}")
            null
        }
    }

    /**
     * 채팅방 정보를 가져오는 함수
     */
    suspend fun getChatRoom(chatRoomId: String): Result<ChatRoom>{
        return try {
            val doc = firestore.collection("chatRooms").document(chatRoomId).get().await()
            val chatRoom = doc.toObject<ChatRoom>()?.copy(id = doc.id)

            if(chatRoom != null){
                Result.success(chatRoom)
            }else{
                Result.failure(Exception("채팅방을 찾을 수 없습니다."))
            }
        }catch (e: Exception){
            Log.e("ChatRepository", "Error getting chat room: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * 사용자 검색 함수
     */
    suspend fun searchUsers(query: String): Result<List<User>>{
        return try {
            val currentUserId = auth.currentUser?.uid?:throw Exception("로그인이 필요합니다")

            val snapshot = firestore.collection("users")
                .orderBy("displayName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get()
                .await()

            val users = snapshot.documents.mapNotNull { doc ->
                doc.toObject<User>()
            }.filter { it.uid != currentUserId }

            Result.success(users)
        }catch (e: Exception){
            Log.e("ChatRepository", "Error searching users: ${e.message}")
            Result.failure(e)
        }
    }
}

