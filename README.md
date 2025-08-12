# 🚀 Android Firebase 채팅 앱 (Kotlin)

실시간 데이터 통신, 사용자 인증, 파일 저장, 푸시 알림 등 현대적인 모바일 앱이 갖춰야 할 핵심 기술들을 Firebase와 Kotlin을 활용하여 구현한 채팅 애플리케이션입니다. MVVM 아키텍처 패턴과 최신 Android 개발 기술을 적용하여 실제 서비스 수준의 품질을 제공합니다.

## ✨ 주요 기능

**실시간 메시징 시스템**은 Firebase Firestore의 실시간 리스너를 활용하여 지연 없는 메시지 송수신을 지원합니다. 사용자가 메시지를 보내면 즉시 모든 참여자에게 전달되며, 네트워크 연결이 불안정한 상황에서도 안정적으로 동작합니다.

**사용자 인증 및 관리** 기능으로는 Firebase Authentication을 통한 이메일 기반 회원가입/로그인, 사용자 프로필 관리, 온라인/오프라인 상태 표시 등을 제공합니다. 사용자 상태 관리를 통해 마지막 접속 시간을 실시간으로 표시합니다.

**멀티미디어 메시지 지원**으로 텍스트뿐만 아니라 이미지, 파일 전송이 가능합니다. Firebase Cloud Storage를 활용하여 안전하고 효율적인 파일 업로드 및 다운로드를 제공하며, 이미지는 자동으로 최적화되어 데이터 사용량을 절약합니다.

**푸시 알림 시스템**은 Firebase Cloud Messaging(FCM)을 통해 앱이 백그라운드에 있거나 종료된 상태에서도 새 메시지 알림을 받을 수 있습니다. 개인 채팅과 그룹 채팅을 구분하여 적절한 알림을 제공합니다.

## 🛠 기술 스택

### **백엔드 서비스 (Firebase)**
- **Firebase Authentication**: 사용자 인증 및 계정 관리
- **Cloud Firestore**: 실시간 데이터베이스 및 메시지 저장
- **Firebase Cloud Storage**: 이미지 및 파일 저장소
- **Firebase Cloud Messaging**: 푸시 알림 서비스

### **프론트엔드 (Android)**
- **Kotlin**: 주 개발 언어
- **MVVM Architecture**: ViewModel, LiveData, Repository 패턴
- **Android Architecture Components**: 생명주기 인식 컴포넌트
- **Kotlin Coroutines**: 비동기 처리 및 성능 최적화
- **Navigation Component**: 화면 간 탐색 관리

### **주요 라이브러리**
- **Glide**: 이미지 로딩 및 캐싱 (v4.15.1)
- **RecyclerView**: 효율적인 메시지 목록 표시
- **Material Design Components**: 현대적인 UI/UX
- **CircleImageView**: 프로필 이미지 표시
- **DiffUtil**: 리스트 데이터 변경 최적화

## 🚀 시작하기

### **필수 요구사항**
- **Android Studio**: Arctic Fox 이상 버전
- **JDK**: 11 이상
- **Android SDK**: API 21 (Android 5.0) 이상
- **Firebase 계정**: Google 계정 필요

### **1단계: 프로젝트 설정**

```bash
git clone https://github.com/LimJunHui-dot/MyChatApp.git
cd MyChatApp
```

### **2단계: Firebase 프로젝트 생성 및 연동**

**Firebase Console 설정:**
1. [Firebase Console](https://console.firebase.google.com)에서 새 프로젝트 생성
2. 프로젝트 이름 입력 및 Google 애널리틱스 설정
3. Android 앱 추가 시 패키지 이름 입력 (예: `com.yourcompany.chatapp`)
4. `google-services.json` 파일 다운로드 후 `app/` 디렉토리에 복사

**Firebase 서비스 활성화:**
- **Authentication**: 이메일/비밀번호 제공업체 활성화
- **Firestore Database**: 테스트 모드로 시작 (개발 단계)
- **Cloud Storage**: 기본 설정으로 활성화
- **Cloud Messaging**: 푸시 알림을 위해 활성화

### **3단계: 의존성 추가**

`app/build.gradle` 파일에 다음 의존성을 추가합니다:

```gradle
dependencies {
    // Firebase SDKs
    implementation 'com.google.firebase:firebase-auth:22.1.1'
    implementation 'com.google.firebase:firebase-firestore:24.7.1'
    implementation 'com.google.firebase:firebase-storage:20.2.1'
    implementation 'com.google.firebase:firebase-messaging:23.2.1'

    // Android Architecture Components
    implementation 'androidx.recyclerview:recyclerview:1.3.1'
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.6.1'
    implementation 'androidx.navigation:navigation-fragment-ktx:2.6.0'
    implementation 'androidx.navigation:navigation-ui-ktx:2.6.0'

    // 이미지 로딩 및 UI 라이브러리
    implementation 'com.github.bumptech.glide:glide:4.15.1'
    implementation 'de.hdodenhof:circleimageview:3.1.0'

    // Kotlin Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
}
```

## 📁 프로젝트 구조

```
app/src/main/java/com/yourpackage/chatapp/
├── data/
│   ├── model/              # 데이터 모델 클래스
│   │   ├── User.kt
│   │   ├── ChatRoom.kt
│   │   └── Message.kt
│   ├── repository/         # 데이터 접근 레이어
│   │   ├── AuthRepository.kt
│   │   ├── ChatRepository.kt
│   │   └── UserRepository.kt
│   └── remote/             # Firebase 서비스 클래스
│       └── FirebaseService.kt
├── ui/
│   ├── auth/               # 인증 관련 화면
│   │   ├── LoginFragment.kt
│   │   ├── SignUpFragment.kt
│   │   └── AuthViewModel.kt
│   ├── chat/               # 채팅 화면
│   │   ├── ChatFragment.kt
│   │   ├── ChatViewModel.kt
│   │   └── MessageAdapter.kt
│   ├── chatlist/           # 채팅 목록 화면
│   │   ├── ChatListFragment.kt
│   │   ├── ChatListViewModel.kt
│   │   └── ChatRoomAdapter.kt
│   └── profile/            # 프로필 화면
│       ├── ProfileFragment.kt
│       └── ProfileViewModel.kt
├── utils/                  # 유틸리티 클래스
│   ├── Constants.kt
│   ├── Extensions.kt
│   └── ImageUtils.kt
└── MainActivity.kt
```

## 🗄️ 데이터베이스 구조 (Firestore)

효율적인 데이터 관리를 위해 다음과 같은 컬렉션 구조를 사용합니다:

```
users (컬렉션)
├── {userId} (문서)
    ├── uid: String
    ├── displayName: String
    ├── email: String
    ├── photoUrl: String
    ├── isOnline: Boolean
    └── lastSeen: Timestamp

chatRooms (컬렉션)
├── {chatRoomId} (문서)
    ├── id: String
    ├── name: String
    ├── participants: Array[String]
    ├── lastMessage: String
    ├── lastMessageTime: Timestamp
    └── isGroup: Boolean

messages (컬렉션)
├── {messageId} (문서)
    ├── id: String
    ├── senderId: String
    ├── senderName: String
    ├── content: String
    ├── timestamp: Timestamp
    ├── type: String (TEXT, IMAGE, FILE)
    ├── chatRoomId: String
    └── imageUrl: String (선택사항)
```

## 🔧 핵심 구현 사항

### **MVVM 아키텍처 패턴**

애플리케이션은 MVVM 패턴을 따라 구현되어 UI 로직과 비즈니스 로직을 분리합니다:

```kotlin
// ChatViewModel.kt
class ChatViewModel(private val chatRepository: ChatRepository) : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    fun loadMessages(chatRoomId: String) {
        chatRepository.getMessagesForChatRoom(chatRoomId).observeForever { messagesList ->
            _messages.value = messagesList
        }
    }

    fun sendMessage(chatRoomId: String, content: String) {
        if (content.trim().isNotEmpty()) {
            chatRepository.sendMessage(chatRoomId, content.trim())
        }
    }
}
```

### **실시간 데이터 동기화**

Firebase Firestore의 실시간 리스너를 활용한 메시지 동기화:

```kotlin
// ChatRepository.kt
fun getMessagesForChatRoom(chatRoomId: String): LiveData<List<Message>> {
    val messagesLiveData = MutableLiveData<List<Message>>()
    
    firestore.collection("messages")
        .whereEqualTo("chatRoomId", chatRoomId)
        .orderBy("timestamp", Query.Direction.ASCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ChatRepository", "Error listening for messages", error)
                return@addSnapshotListener
            }
            
            val messages = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Message::class.java)
            } ?: listOf()
            
            messagesLiveData.postValue(messages)
        }
    
    return messagesLiveData
}
```

## 🔒 보안 설정 (필수)

**배포 전 반드시 설정해야 하는 Firebase Security Rules:**

```javascript
// Firestore Security Rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // 사용자는 자신의 문서만 읽고 쓸 수 있음
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // 채팅방 참여자만 메시지에 접근 가능
    match /messages/{messageId} {
      allow read, write: if request.auth != null && 
        request.auth.uid in get(/databases/$(database)/documents/chatRooms/$(resource.data.chatRoomId)).data.participants;
    }
    
    // 채팅방 참여자만 채팅방 정보에 접근 가능
    match /chatRooms/{chatRoomId} {
      allow read, write: if request.auth != null && 
        request.auth.uid in resource.data.participants;
    }
  }
}
```

## 🚀 고급 기능 확장

기본 기능 완성 후 다음 기능들을 추가할 수 있습니다:

**이미지 전송 기능**은 Firebase Cloud Storage를 활용하여 구현할 수 있습니다. 갤러리에서 이미지를 선택하거나 카메라로 촬영한 이미지를 업로드하고, 다운로드 URL을 메시지에 포함시켜 전송합니다.

**푸시 알림 고도화**는 Firebase Cloud Functions와 연동하여 서버 측에서 자동으로 알림을 발송하도록 구현할 수 있습니다. 사용자별 알림 설정, 그룹 채팅 알림 등을 지원합니다.

**사용자 상태 관리**로는 실시간 온라인/오프라인 상태, 타이핑 상태, 메시지 읽음 상태 등을 표시하여 더 풍부한 사용자 경험을 제공할 수 있습니다.

## 💡 개발 팁 및 주의사항

**성능 최적화**를 위해 다음 사항들을 고려하세요:
- RecyclerView에서 DiffUtil을 사용하여 효율적인 데이터 업데이트 구현
- Glide의 캐싱 기능을 적극 활용하여 이미지 로딩 최적화
- Firestore 쿼리 최적화 및 인덱스 활용으로 불필요한 데이터 전송 최소화

**에러 처리**는 네트워크 연결 끊김, 권한 없음, 파일 업로드 실패 등 다양한 상황에 대비해야 합니다. try-catch 블록과 Result 클래스를 활용하여 안정적인 에러 처리를 구현하고, 사용자에게 적절한 피드백을 제공하세요.

**메모리 관리**는 RecyclerView의 ViewHolder 패턴을 적절히 활용하고, 이미지 로딩 시 메모리 누수를 방지하기 위해 생명주기를 고려한 코드를 작성하세요.

## 📚 학습 자료 및 참고 문서

개발 과정에서 참고할 수 있는 주요 자료들:

- **Firebase 공식 문서**: [Firebase Documentation](https://firebase.google.com/docs)
- **Android Developers**: [Android Architecture Guide](https://developer.android.com/topic/architecture)
- **Kotlin Coroutines**: [Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- **GitHub 예제**: "Android Firebase Chat App Kotlin"으로 검색하여 다양한 오픈소스 프로젝트 참고

