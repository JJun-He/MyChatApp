package com.example.mychatapp.data.service

import android.util.Log
import com.example.mychatapp.data.api.ChatCompletionsRequest
import com.example.mychatapp.data.api.ChatMessage
import com.example.mychatapp.data.api.OpenAIApi
import com.example.mychatapp.data.api.OpenAIError
import com.example.mychatapp.data.model.Message
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class OpenAIService {

    companion object{
        private const val TAG = "OpenAIService"
        private const val BASE_URL = "https://api.openapi.com/"
        private const val TIMEOUT_SECONDS = 30L
    }

    private val api: OpenAIApi
    private val gson = Gson()

    // 보안 주의: 개발/테스트 전용
    private val apiKey: String by lazy {
        try {
            // 리플렉션으로 BuildConfig 접근
            val buildConfigClass = Class.forName("com.example.mychatapp.BuildConfig")
            val field = buildConfigClass.getDeclaredField("OPENAI_API_KEY")
            field.get(null) as String
        } catch (e: Exception) {
            Log.e("OpenAIService", "BuildConfig 접근 실패: ${e.message}")
            ""
        }
    }

    init {
        // HTTP 로깅 인터셉터(디버깅용)
        val logging = HttpLoggingInterceptor{message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // OkHttp 클라이언트 설정
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        // Retrofit 설정
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(OpenAIApi::class.java)
    }

    /**
     * AI 요청 타입 분석
     */
    private fun analyzeRequestType(prompt: String): String{
        val lowerPrompt = prompt.lowercase()
        return when {
            lowerPrompt.contains("번역") || lowerPrompt.contains("translate") -> "translate"
            lowerPrompt.contains("문법") || lowerPrompt.contains("grammar") -> "grammar"
            lowerPrompt.contains("요약") || lowerPrompt.contains("summary") -> "summary"
            lowerPrompt.contains("코드") || lowerPrompt.contains("code") -> "code"
            else -> "general"
        }
    }

    /**
     * 요청 타입별 시스템 프롬포트 생성
     */
    private fun getSystemPrompt(requestType: String): String {
        return when (requestType) {
            "translation" -> """
                당신은 전문 번역가입니다. 정확하고 자연스러운 번역을 제공하며, 
                문화적 맥락도 고려해주세요. 번역 결과와 간단한 설명을 제공해주세요.
                
                답변 형식:
                **번역 결과:** [번역된 텍스트]
                **설명:** [간단한 번역 설명]
            """.trimIndent()

            "grammar" -> """
                당신은 언어 전문가입니다. 문법 오류를 찾아 수정하고, 
                더 자연스러운 표현을 제안해주세요.
                
                답변 형식:
                **수정된 문장:** [올바른 문장]
                **설명:** [수정 이유와 문법 설명]
            """.trimIndent()

            "summary" -> """
                당신은 대화 요약 전문가입니다. 주요 내용을 간결하고 명확하게 요약하며, 
                중요한 결정사항이나 액션 아이템이 있다면 별도로 강조해주세요.
                
                답변 형식:
                **요약:** [핵심 내용 요약]
                **주요 포인트:** [중요한 사항들]
            """.trimIndent()

            "code" -> """
                당신은 프로그래밍 전문가입니다. 코드 문제를 분석하고 명확한 해결책을 제시하며, 
                개선된 코드와 설명을 함께 제공해주세요.
                
                답변 형식:
                **문제 분석:** [문제점 설명]
                **해결 방법:** [구체적인 해결책]
                **개선된 코드:** [수정된 코드 예시]
            """.trimIndent()

            else -> """
                당신은 친근하고 도움이 되는 AI 어시스턴트입니다. 
                질문에 정확하고 유용한 답변을 제공하며, 이모지를 적절히 사용하여 
                친근한 분위기를 만들어주세요. 답변은 간결하면서도 충분한 정보를 포함해주세요.
            """.trimIndent()
        }
    }

    /**
     *  ChatGPT 응답 생성
     */
    suspend fun getChatResponse(
        prompt: String,
        recentMessages: List<Message> = emptyList()
    ): Result<String>{
        return try{
            Log.d(TAG, "AI 요청 시작: ${prompt.take(50)}...")

            // API 키 유효성 검사
            if(apiKey.startsWith("sk-proj") || apiKey.isBlank()){
                return Result.failure(Exception("OpenAI API 키가 설정되지 않았습니다. OpenAIService.kt에서 실제 API 키로 교체해주세요."))
            }

            val requestType = analyzeRequestType(prompt)
            val systemPrompt = getSystemPrompt(requestType)

            // 메시지 구성
            val messages = mutableListOf<ChatMessage>()

            // 1. 시스템 프롬프트
            messages.add(ChatMessage(role = "system", content = systemPrompt))

            // 2. 최근 대화 맥락 (최대 3개)
            recentMessages.takeLast(3).forEach { message ->
                val role = if(message.senderId == "AI_ASSISTANT") "assistant" else "user"
                val content = "${message.senderName} : ${message.content}"
                messages.add(ChatMessage(role = role, content = content))
            }

            // 3. 현재 질문
            messages.add(ChatMessage(role = "user", content = prompt))

            // API 요청 생성
            val request = ChatCompletionsRequest(
                model = "gpt-3.5-turbo",
                messages = messages,
                max_tokens = 500,
                temperature = 0.7
            )

            Log.d(TAG, "Open AI API 호출 중,,,")

            // API 호출
            val response = api.createChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            if(response.isSuccessful){
                val aiResponse = response.body()?.choices?.firstOrNull()?.message?.content?: "응답을 생성할 수 없습니다."

                val usage = response.body()?.usage
                Log.d(TAG, "AI 응답 성공: ${aiResponse.take(50)}...")
                Log.d(TAG, "토큰 사용량: ${usage?.total_tokens ?: 0}")

                Result.success(aiResponse)
            }else{
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "OpenAI API 오류: ${response.code()} - $errorBody")

                // 에러 파싱 시도
                val errorMessage = try{
                    val error = gson.fromJson(errorBody, OpenAIError::class.java)
                    error.error.message
                }catch (e: Exception){
                    "OpenAI API 오류 (${response.code()}"
                }

                Result.failure(Exception(errorMessage))
            }

        }catch (e: Exception){
            Log.e(TAG, "AI 요청 실패: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * API 키 유효성 검사
     */
    fun isAPiKeyValid(): Boolean{
        return apiKey.startsWith("sk-") && !apiKey.contains("여기에")
    }

    /**
     * 간단한 연결 테스트
     */
    suspend fun testConnection() : Result<String>{
        return getChatResponse("안녕하세요! 연결 테스트입니다.")
    }
}