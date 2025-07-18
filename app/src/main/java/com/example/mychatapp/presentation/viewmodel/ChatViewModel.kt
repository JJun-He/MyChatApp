package com.example.mychatapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.privacysandbox.ads.adservices.adid.AdId
import com.example.mychatapp.data.model.ChatRoom
import com.example.mychatapp.data.model.Message
import com.example.mychatapp.data.model.MessageType
import com.example.mychatapp.data.model.User
import com.example.mychatapp.data.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


/**
 * 채팅 관련 UI 상태를 관리하는 ViewModel
 */
class ChatViewModel(
    private var chatRepository: ChatRepository
): ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // 채팅방 목록 관련 상태
    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList());
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()

    // 메시지 관련 상태
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    // 현재 채팅방 정보
    private val _currentChatRoom = MutableStateFlow<ChatRoom?>(null)
    val currentChatRoom: StateFlow<ChatRoom?> = _currentChatRoom.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 에러 메시지
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // 사용자 검색 관련 상태
    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    val currentUserId: String? = auth.currentUser?.uid

    init {
        loadChatRooms()
    }

    /**
     * 채팅방 목록을 로드하는 함수
     */
    private fun loadChatRooms(){
        viewModelScope.launch {
            _isLoading.value = true

            chatRepository.getChatRooms().collect { chatRooms ->
                _chatRooms.value = chatRooms
                _isLoading.value = false
            }
        }
    }

    /**
     *  특정 채팅방의 메시지를 로드하는 함수
     */
    fun loadMessages(chatRoomId: String){
        viewModelScope.launch {
            // 현재 채팅방 정보 로드
            val chatRoomResult = chatRepository.getChatRoom(chatRoomId)
            chatRoomResult.onSuccess { chatRoom ->
                _currentChatRoom.value = chatRoom
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "채팅방 정보를 불러올 수 없습니다"
            }

            // 메시지 목록 실시간 로드
            chatRepository.getMessages(chatRoomId).collect { messages ->
                _messages.value = messages
            }
        }
    }

    /**
     * 메시지를 전송하는 함수
     */
    fun sendMessage(chatRoomId: String, content: String){
        if(content.trim().isEmpty()) return

        viewModelScope.launch {
            val result = chatRepository.sendMessages(
                chatRoomId = chatRoomId,
                content = content.trim(),
                type = MessageType.TEXT
            )

            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "메시지 전송에 실패했습니다."
            }
        }
    }

    /**
     * 새로운 채팅방을 생성하는 함수
     */
    fun createChatRoom(
        name: String,
        participantIds: List<String>,
        isGroup: Boolean = true,
        onSuccess: (String) -> Unit
    ){
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = chatRepository.createChatRoom(
                name = name,
                participantIds = participantIds,
                isGroup = isGroup
            )

            _isLoading.value = false

            result.onSuccess { chatRoom ->
                onSuccess(chatRoom.id)
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "채팅방 생성에 실패했습니다."
            }
        }
    }

    /**
     * 개인 채팅방을 생성하는 함수
     */
    fun createPrivateChat(otherUserId: String, onSuccess: (String) -> Unit){
        createChatRoom(
            name = "",
            participantIds = listOf(otherUserId),
            isGroup = false,
            onSuccess = onSuccess
        )
    }

    /**
     * 사용자를 검색하는 함수
     */
    fun searchUsers(query: String){
        if(query.isBlank()){
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            val result = chatRepository.searchUsers(query)
            result.onSuccess { users ->
                _searchResults.value = users
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "사용자 검색에 실패했습니다."
            }
        }
    }

    /**
     * 검색 결과를 초기화하는 함수
     */
    fun clearSearchResults(){
        _searchResults.value = emptyList()
    }

    /**
     * 에러 메시지를 초기화하는 함수
     */
    fun clearErrorMessage(){
        _errorMessage.value = null
    }

    /**
     * 현재 사용자가 메시지의 작성자인지 확인하는 함수
     */
    fun isMyMessage(message: Message): Boolean{
        return message.senderId == currentUserId
    }

    /**
     * 채팅방 제목을 가져오는 함수
     */
    fun getChatRoomTitle(): String{
        val chatRoom = _currentChatRoom.value ?: return "채팅"
        return chatRoom.getDisplayName(currentUserId ?: "")
    }
}

/**
 * ChatViewModel을 생성하기 위한 팩토리 클래스
 */
class ChatViewModelFactory(
    private val chatRepository: ChatRepository
): androidx.lifecycle.ViewModelProvider.Factory{

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ChatViewModel::class.java)){
            return ChatViewModel(chatRepository) as T
        }
        throw IllegalArgumentException("UNKNOWN ViewModel class")
    }
}

