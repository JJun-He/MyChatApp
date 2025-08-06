package com.example.mychatapp.data.api


import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST


/**
 * OpenAI Chat Completions API 요청 데이터 클래스
 */
data class ChatCompletionsRequest(
    val model: String = "gpt-3.5-turbo", // 가장 인기 있는 모델
    val messages: List<ChatMessage>,
    val max_tokens: Int = 500, // 응답 길이 제한
    val temperature: Double = 0.7 // 창의성 수준 (0.0~1.0)
)

/**
 * 채팅 메시지 데이터 클래스
 */
data class ChatMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

/**
 * OpenAI API 응답 데이터 클래스
 */
data class ChatCompletionResponse(
    val choices: List<ChatChoice>,
    val usage: Usage? = null // 토큰 사용량 정보
)

data class ChatChoice(
    val message: ChatMessage,
    val finish_reason: String? = null
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens : Int?
)

/**
 * OpenAI API 에러 응답
 */
data class OpenAIError(
    val error: ErrorDetail
)

data class ErrorDetail(
    val message: String,
    val type: String,
    val code: String? = null
)

/**
 * Retrofit 인터페이스
 */
interface OpenAIApi{
    @POST("v1/chat/completions")
    suspend fun createChatCompletion(
        @Header("Authorization") authorization: String,
        @Header("Content-Type") contentType: String = "application/json",
        @Body request: ChatCompletionsRequest
    ): Response<ChatCompletionResponse>
}