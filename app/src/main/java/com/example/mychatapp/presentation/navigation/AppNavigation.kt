package com.example.mychatapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mychatapp.data.model.AuthState
import com.example.mychatapp.data.repository.AuthRepository
import com.example.mychatapp.data.repository.ChatRepository
import com.example.mychatapp.presentation.screen.AuthScreen
import com.example.mychatapp.presentation.screen.ChatRoomListScreen
import com.example.mychatapp.presentation.viewmodel.AuthViewModel
import com.example.mychatapp.presentation.viewmodel.AuthViewModelFactory
import com.example.mychatapp.presentation.viewmodel.ChatViewModel
import com.example.mychatapp.presentation.viewmodel.ChatViewModelFactory


/**
 * 앱의 전체 네비게이션을 관리하는 Composable
 */
@Composable
fun AppNavigation (
    authRepository: AuthRepository,
    chatRepository: ChatRepository,
    navController: NavHostController = rememberNavController()
){
    // AuthViewModel 생성
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(authRepository)
    )

    // ChatViewModel 생성
    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(chatRepository)
    )

    // 인증 상태 관찰
    val authState by authViewModel.authState.collectAsStateWithLifecycle()

    // 인증 상태에 따라 시작 화면 결정
    val startDestination = when(authState){
        is AuthState.Authenticated -> "chat_room_list"
        else -> "auth"
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ){
        // 인증 화면
        composable("auth"){
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate("chat_room_list"){
                        popUpTo("auth"){inclusive = true}
                    }
                }
            )
        }

        // 채팅 화면 목록
        composable("chat_room_list"){
            ChatRoomListScreen(
                chatViewModel = chatViewModel,
                authViewModel = authViewModel,
                onChatRoomClick = {chatRoomId ->
                    navController.navigate("chat/$chatRoomId")
                },
                onCreateChatClick = {
                    navController.navigate("create_chat")
                },
                onSignOut = {
                    navController.navigate("auth"){
                        popUpTo("chat_room_list"){inclusive = true}
                    }
                }
            )
        }

        // 채팅 화면
        composable("chat/{chatRoomId}"){backStackEntry ->
            val chatRoomId = backStackEntry.arguments?.getString("chatRoomId")?: ""

            ChatScreen(
                chatRoomId = chatRoomId,
                chatViewModel = chatViewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 채팅방 생성 화면
        composable("create_chat"){
            CreateChatScreen(
                chatViewModel = chatViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onChatRoomCreated = { chatRoomId ->
                    navController.navigate("chat/$chatRoomId"){
                        popUpTo("create_chat"){inclusive = true}
                    }
                }
            )
        }
    }
}