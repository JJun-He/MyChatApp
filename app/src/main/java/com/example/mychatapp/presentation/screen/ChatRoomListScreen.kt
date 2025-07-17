package com.example.mychatapp.presentation.screen

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mychatapp.data.model.ChatRoom
import com.example.mychatapp.presentation.viewmodel.AuthViewModel
import com.example.mychatapp.presentation.viewmodel.ChatViewModel
import org.w3c.dom.Text


/**
 * 채팅방 목록을 표시하는 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomListScreen(
    chatViewModel : ChatViewModel,
    authViewModel : AuthViewModel,
    onChatRoomClick : (String) -> Unit,
    onCreateChatClick : () -> Unit,
    onSignOut: () -> Unit
) {
    val chatRooms by chatViewModel.chatRooms.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by chatViewModel.errorMessage.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

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
                    Text("채팅")
                },
                actions = {
                    IconButton(onClick = onCreateChatClick) {
                        Icon(Icons.Default.Add, contentDescription = "새 채팅")
                    }
                    IconButton(onClick = {
                        authViewModel.signOut()
                        onSignOut()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "로그아웃")
                    }
                }
            )
        }
    ){
        paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if(isLoading){
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if(chatRooms.isEmpty() && !isLoading){
                // 빈 상태 표시
                Column (
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ){
                    Text(
                        text = "\uD83D\uDCAC",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "아직 채팅방이 없습니다",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "새로운 채팅을 시작해보세요!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = onCreateChatClick) {
                        Text("새 채팅 시작")
                    }
                }
            }else{
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(chatRooms){chatRoom ->
                        ChatRoomItem(
                            chatRoom = chatRoom,
                            currentUserId = currentUser?.uid?: "",
                            onClick = {onChatRoomClick(chatRoom.id)}
                        )
                    }
                }
            }
        }
    }
}

/**
 * 채팅방 항목 Composable
 */
@Composable
fun ChatRoomItem(
    chatRoom: ChatRoom,
    currentUserId: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 채팅방 아이콘 (프로필 사진 자리)
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chatRoom.getDisplayName(currentUserId).take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = chatRoom.getDisplayName(currentUserId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = chatRoom.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 시간 표시
            chatRoom.lastMessageTime?.let { timestamp ->
                Text(
                    text = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        .format(timestamp.toDate()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}