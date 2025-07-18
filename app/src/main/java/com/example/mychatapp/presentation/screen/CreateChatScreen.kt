package com.example.mychatapp.presentation.screen

import android.R
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mychatapp.data.model.User
import com.example.mychatapp.presentation.viewmodel.ChatViewModel
import okhttp3.internal.userAgent
import okio.utf8Size


/**
 * 새 채팅방 생성 화면
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateChatScreen(
    chatViewModel: ChatViewModel,
    onBackClick: () -> Unit,
    onChatRoomCreated: (String)-> Unit,
){
    val searchResults by chatViewModel.searchResults.collectAsStateWithLifecycle()
    val isLoading by chatViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by chatViewModel.errorMessage.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedUsers by remember { mutableStateOf(setOf<User>()) }
    var chatRoomName by remember { mutableStateOf(" ") }
    var showCreateDialog by remember { mutableStateOf(false) }

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
                title = { Text("새 채팅") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로 가기")
                    }
                },
                actions = {
                    if(selectedUsers.isNotEmpty()){
                        IconButton(onClick = {showCreateDialog = true}) {
                            Icon(Icons.Default.Check, contentDescription = "채팅방 생성")
                        }
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
            // 검색 입력 필드
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    chatViewModel.searchUsers(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = {Text("사용자 이름 또는 이메일 검색")},
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "검색")
                },
                singleLine = true
            )

            if(isLoading){
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 선택된 사용자 표시
            if(selectedUsers.isNotEmpty()){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "선택된 사용자(${selectedUsers.size}명",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        selectedUsers.forEach { user ->
                            Text(
                                text = "${user.displayName}(${user.email})",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // 검색 결과 목록
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(searchResults){user ->
                    UserItem(
                        user = user,
                        isSelected = selectedUsers.contains(user),
                        onClick = {
                            selectedUsers = if(selectedUsers.contains(user)){
                                selectedUsers - user
                            }else{
                                selectedUsers + user
                            }
                        }
                    )
                }
            }
        }
    }

    // 채팅방 생성 다이얼로그
    if(showCreateDialog){
        AlertDialog(
            onDismissRequest = {showCreateDialog = false},
            title = {Text("채팅방 생성")},
            text = {
                Column {
                    if(selectedUsers.size > 1){
                        OutlinedTextField(
                            value = chatRoomName,
                            onValueChange = {chatRoomName = it},
                            label = {Text("채팅방 이름")},
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Text("${selectedUsers.size}명과 채팅을 시작하시겠습니까?")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val isGroup = selectedUsers.size > 1
                        val name = if(isGroup) chatRoomName else ""

                        chatViewModel.createChatRoom(
                            name = name,
                            participantIds = selectedUsers.map{it.uid},
                            isGroup = isGroup,
                            onSuccess = {chatRoomId ->
                                onChatRoomCreated(chatRoomId)
                            }
                        )
                        showCreateDialog = false
                    }
                ) {
                    Text("생성")
                }
            },
            dismissButton = {
                TextButton(onClick = {showCreateDialog = false}) {
                    Text("취소")
                }
            }
        )
    }
}

/**
 *  사용자 항목 Composable
 */
@Composable
fun UserItem(
    user: User,
    isSelected: Boolean,
    onClick: () -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if(isSelected){
                MaterialTheme.colorScheme.primaryContainer
            }else{
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 사용자 아이콘
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ){
                    Text(
                        text = user.displayName.take(1).uppercase(),
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
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if(isSelected){
                Icon(
                    Icons.Default.Check,
                    contentDescription = "선택됨",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}