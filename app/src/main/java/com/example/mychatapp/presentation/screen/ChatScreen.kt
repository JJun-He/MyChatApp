package com.example.mychatapp.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mychatapp.data.model.Message
import com.example.mychatapp.presentation.viewmodel.ChatViewModel

/**
 * 채팅 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRoomId: String,
    chatViewModel: ChatViewModel,
    onBackClick: () -> Unit
){
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by chatViewModel.errorMessage.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var messageInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // 메시지 로드
    LaunchedEffect(chatRoomId) {
        chatViewModel.loadMessages(chatRoomId)
    }

    // 새 메시지가 추가되면 스크롤을 맨 아래로
    LaunchedEffect(messages.size) {
        if(messages.isNotEmpty()){
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // 에러 메시지 표시
    LaunchedEffect(errorMessage) {
        errorMessage?.let{message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            chatViewModel.clearErrorMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(chatViewModel.getChatRoomTitle())
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                }
            )
        }
    ) {paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if(isLoading){
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 메시지 목록
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages){message ->
                    MessageBubble(
                        message = message,
                        isCurrentUser = chatViewModel.isMyMessage(message)
                    )
                }
            }

            // 메시지 입력 영역
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = {messageInput = it},
                        modifier = Modifier.weight(1f),
                        placeholder = {Text("메시지를 입력하세요...")},
                        maxLines = 4,
                        shape = RoundedCornerShape(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    FloatingActionButton(
                        onClick = {
                            if(messageInput.isNotBlank()){
                                chatViewModel.sendMessage(chatRoomId, messageInput.trim())
                                messageInput = " "
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = "전송",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 메시지 버블 Composable
 */
@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean
){
    val backgroundColor = if(isCurrentUser){
        MaterialTheme.colorScheme.primary
    }else{
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isCurrentUser){
        MaterialTheme.colorScheme.onPrimary
    }else{
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if(isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        if(isCurrentUser){
            Spacer(modifier = Modifier.width(64.dp))
        }

        Column(
            horizontalAlignment = if(isCurrentUser) Alignment.End else Alignment.Start
        ) {
            if(!isCurrentUser){
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if(isCurrentUser) 16.dp else 4.dp,
                    bottomEnd = if(isCurrentUser) 4.dp else 16.dp
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        if(!isCurrentUser){
            Spacer(modifier = Modifier.width(64.dp))
        }
    }
}