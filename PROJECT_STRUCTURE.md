# 📁 Project Structure Visual Guide

## Complete File Tree

```
AndroidInterviewPrep/
│
├── app/
│   ├── build.gradle.kts                     # App-level build configuration
│   └── src/main/
│       ├── AndroidManifest.xml              # App manifest
│       │
│       └── java/com/example/interviewprep/
│           │
│           ├── 📦 data/                      # DATA LAYER
│           │   ├── local/                    # Local data source (Room)
│           │   │   ├── entity/
│           │   │   │   └── UserEntity.kt     # Room entity + mappers
│           │   │   ├── dao/
│           │   │   │   └── UserDao.kt        # Room DAO
│           │   │   └── AppDatabase.kt        # Room database
│           │   │
│           │   ├── remote/                   # Remote data source (API)
│           │   │   ├── dto/
│           │   │   │   └── UserDto.kt        # API DTO + mappers
│           │   │   └── ApiService.kt         # Retrofit interface
│           │   │
│           │   ├── paging/
│           │   │   └── UserRemoteMediator.kt # Paging 3 mediator
│           │   │
│           │   └── repository/
│           │       └── UserRepositoryImpl.kt # Repository implementation
│           │
│           ├── 📦 domain/                    # DOMAIN LAYER
│           │   ├── model/
│           │   │   └── User.kt               # Domain entity (pure Kotlin)
│           │   ├── repository/
│           │   │   └── UserRepository.kt     # Repository interface
│           │   └── usecase/
│           │       └── GetUsersUseCase.kt    # Business logic
│           │
│           ├── 📦 presentation/              # PRESENTATION LAYER
│           │   └── users/
│           │       ├── UserListContract.kt   # MVI (State/Event/Effect)
│           │       ├── UserListViewModel.kt  # ViewModel with MVI
│           │       └── UserListScreen.kt     # Compose UI
│           │
│           ├── 📦 di/                        # DEPENDENCY INJECTION
│           │   ├── NetworkModule.kt          # Retrofit, OkHttp
│           │   ├── DatabaseModule.kt         # Room
│           │   └── RepositoryModule.kt       # Repository bindings
│           │
│           ├── InterviewPrepApp.kt           # Application class
│           └── MainActivity.kt               # Main activity
│
├── build.gradle.kts                         # Project-level build config
├── settings.gradle.kts                      # Project settings
├── gradle.properties                        # Gradle properties
│
└── 📄 Documentation/
    ├── README.md                            # Complete guide
    ├── CHEATSHEET.md                        # Quick reference
    ├── INTERVIEW_QA.md                      # Q&A
    └── PROJECT_STRUCTURE.md                 # This file
```

---

## Layer Dependencies

```
┌─────────────────────────────────────────────────┐
│              PRESENTATION LAYER                 │
│  ┌──────────────────────────────────────────┐   │
│  │         UserListScreen.kt                │   │
│  │              (Compose UI)                │   │
│  └──────────────────┬───────────────────────┘   │
│                     │                           │
│                     ↓                           │
│  ┌──────────────────────────────────────────┐   │
│  │       UserListViewModel.kt               │   │
│  │         (MVI State/Events)               │   │
│  └──────────────────┬───────────────────────┘   │
└────────────────────│────────────────────────────┘
                     │ depends on
                     ↓
┌─────────────────────────────────────────────────┐
│               DOMAIN LAYER                      │
│  ┌──────────────────────────────────────────┐   │
│  │         GetUsersUseCase.kt               │   │
│  │        (Business Logic)                  │   │
│  └──────────────────┬───────────────────────┘   │
│                     │                           │
│                     ↓                           │
│  ┌──────────────────────────────────────────┐   │
│  │       UserRepository (interface)         │   │
│  │                                          │   │
│  └──────────────────────────────────────────┘   │
│                                                 │
│  ┌──────────────────────────────────────────┐   │
│  │           User.kt                        │   │
│  │        (Domain Model)                    │   │
│  └──────────────────────────────────────────┘   │
└────────────────────│────────────────────────────┘
                     │ implemented by
                     ↓
┌─────────────────────────────────────────────────┐
│                DATA LAYER                       │
│  ┌──────────────────────────────────────────┐   │
│  │      UserRepositoryImpl.kt               │   │
│  │     (Repository Implementation)          │   │
│  └──────┬────────────────────────┬──────────┘   │
│         │                        │              │
│         ↓                        ↓              │
│  ┌──────────────┐         ┌──────────────┐      │
│  │  ApiService  │         │   UserDao    │      │
│  │  (Retrofit)  │         │   (Room)     │      │
│  └──────────────┘         └──────────────┘      │
│         ↓                        ↓              │
│  ┌──────────────┐         ┌──────────────┐      │
│  │   UserDto    │         │ UserEntity   │      │
│  │ (API Model)  │         │ (DB Model)   │      │
│  └──────────────┘         └──────────────┘      │
│                                                 │
│  ┌──────────────────────────────────────────┐   │
│  │      UserRemoteMediator.kt               │   │
│  │    (Paging 3 Coordinator)                │   │
│  └──────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
```

---

## Data Flow (Paging 3)

```
┌──────────────┐
│   User       │
│   Scrolls    │
└──────┬───────┘
       │
       ↓
┌──────────────────────────────┐
│  LazyColumn                  │
│  (collectAsLazyPagingItems)  │
└──────────┬───────────────────┘
           │
           ↓
┌──────────────────────────────┐
│  ViewModel                   │
│  Flow<PagingData<User>>      │
│  (cachedIn viewModelScope)   │
└──────────┬───────────────────┘
           │
           ↓
┌──────────────────────────────┐
│  Use Case                    │
│  repository.getUsers()       │
└──────────┬───────────────────┘
           │
           ↓
┌──────────────────────────────┐
│  Repository                  │
│  Pager + RemoteMediator      │
└──────────┬───────────────────┘
           │
           ↓
┌──────────────────────────────┐
│  RemoteMediator              │
│  ┌────────────┐              │
│  │ Load Type? │              │
│  └─────┬──────┘              │
│        │                     │
│  ┌─────▼──────┐              │
│  │  REFRESH?  │──Yes─┐       │
│  └────────────┘      │       │
│        │No           │       │
│  ┌─────▼──────┐      │       │
│  │  APPEND?   │──Yes─┤       │
│  └────────────┘      │       │
│                      │       │
│  ┌───────────────────▼────┐  │
│  │  Fetch from API        │  │
│  └───────────┬────────────┘  │
│              │               │
│  ┌───────────▼────────────┐  │
│  │  Save to Room          │  │
│  └────────────────────────┘  │
└──────────┬───────────────────┘
           │
           ↓
┌──────────────────────────────┐
│  Room Database               │
│  PagingSource                │
└──────────┬───────────────────┘
           │
           ↓ (emits PagingData)
┌──────────────────────────────┐
│  Back to UI                  │
│  LazyColumn updates          │
└──────────────────────────────┘
```

---

## MVI State Flow

```
┌─────────────────────────────────────┐
│           UI (Composable)           │
│                                     │
│  val state by viewModel            │
│      .state.collectAsState()        │
│                                     │
│  UserItem(                          │
│    onClick = {                      │
│      viewModel.onEvent(             │
│        UserEvent.Click(id)          │
│      )                              │
│    }                                │
│  )                                  │
└─────────────┬───────────────────────┘
              │
              │ Sends Event
              ↓
┌─────────────────────────────────────┐
│          ViewModel                  │
│                                     │
│  fun onEvent(event: UserEvent) {    │
│    when (event) {                   │
│      UserEvent.Click ->              │
│        handleClick()                │
│    }                                │
│  }                                  │
│                                     │
│  private fun handleClick() {        │
│    _state.update {                  │
│      it.copy(loading = true)        │
│    }                                │
│    // ... do work ...               │
│    _effect.send(                    │
│      Effect.Navigate(id)            │
│    )                                │
│  }                                  │
└─────────────┬───────────────────────┘
              │
              │ Updates State
              ↓
┌─────────────────────────────────────┐
│     StateFlow<UiState>              │
│                                     │
│  data class UiState(                │
│    val users: Flow<PagingData>,     │
│    val loading: Boolean,            │
│    val error: String?               │
│  )                                  │
└─────────────┬───────────────────────┘
              │
              │ Emits to
              ↓
┌─────────────────────────────────────┐
│        UI Recomposes                │
│  (with new state automatically)     │
└─────────────────────────────────────┘
```

---

## Dependency Injection Flow

```
┌──────────────────────────────────────┐
│     Application Startup              │
│                                      │
│  @HiltAndroidApp                     │
│  class InterviewPrepApp : App()      │
└──────────────┬───────────────────────┘
               │
               │ Hilt generates code
               ↓
┌──────────────────────────────────────┐
│     Dagger/Hilt Graph                │
│                                      │
│  Modules:                            │
│  • NetworkModule                     │
│  • DatabaseModule                    │
│  • RepositoryModule                  │
└──────────────┬───────────────────────┘
               │
               │ Provides dependencies
               ↓
┌──────────────────────────────────────┐
│        Components                    │
│                                      │
│  SingletonComponent:                 │
│  • Retrofit                          │
│  • ApiService                        │
│  • AppDatabase                       │
│  • UserRepository                    │
│                                      │
│  ViewModelComponent:                 │
│  • GetUsersUseCase                   │
└──────────────┬───────────────────────┘
               │
               │ Injects into
               ↓
┌──────────────────────────────────────┐
│      @HiltViewModel                  │
│      class UserListViewModel         │
│      @Inject constructor(            │
│        private val useCase           │
│      )                               │
└──────────────┬───────────────────────┘
               │
               │ Used in
               ↓
┌──────────────────────────────────────┐
│     Composable                       │
│                                      │
│  @Composable                         │
│  fun Screen(                         │
│    vm: UserViewModel = hiltViewModel()│
│  )                                   │
└──────────────────────────────────────┘
```

---

## File Responsibilities

### Data Layer

**UserEntity.kt**
- Room database table definition
- Mappers to/from domain model
- Database-specific annotations

**UserDao.kt**
- Database operations (CRUD)
- Returns PagingSource for Paging 3
- Suspend functions for async operations

**AppDatabase.kt**
- Room database holder
- Defines all entities
- Provides DAOs

**UserDto.kt**
- API response model
- Mappers to domain model
- Gson/serialization annotations

**ApiService.kt**
- Retrofit interface
- API endpoint definitions
- HTTP method annotations

**UserRemoteMediator.kt**
- Coordinates network + database
- Handles LoadType logic
- Saves API responses to Room

**UserRepositoryImpl.kt**
- Implements domain repository interface
- Coordinates data sources
- Handles caching strategy

### Domain Layer

**User.kt**
- Pure Kotlin model
- No Android dependencies
- Business entity

**UserRepository.kt**
- Interface defining data operations
- Abstraction over data sources
- Used by use cases

**GetUsersUseCase.kt**
- Business logic
- Can combine multiple repositories
- Single responsibility

### Presentation Layer

**UserListContract.kt**
- UiState (what UI displays)
- UiEvent (user actions)
- UiEffect (one-time events)

**UserListViewModel.kt**
- MVI implementation
- State management
- Event handling
- Effect emission

**UserListScreen.kt**
- Compose UI
- Observes state
- Sends events
- Handles effects

### DI Layer

**NetworkModule.kt**
- Provides Retrofit
- Provides ApiService
- Provides OkHttpClient

**DatabaseModule.kt**
- Provides Room database
- Provides DAOs

**RepositoryModule.kt**
- Binds repository interfaces
- Uses @Binds for efficiency

---

## Key Files for Interview

If you only have time to review a few files, focus on these:

1. **UserListViewModel.kt** - Shows MVI pattern
2. **UserRepositoryImpl.kt** - Shows Paging 3 setup
3. **UserRemoteMediator.kt** - Shows network + cache coordination
4. **UserListScreen.kt** - Shows Compose + Paging integration
5. **NetworkModule.kt** - Shows Hilt setup

---

## Quick Navigation

From any file, here's how to navigate:

- **From UserListScreen.kt** → UserListViewModel.kt (ViewModel)
- **From UserListViewModel.kt** → GetUsersUseCase.kt (Use Case)
- **From GetUsersUseCase.kt** → UserRepository.kt (Interface)
- **From UserRepository.kt** → UserRepositoryImpl.kt (Implementation)
- **From UserRepositoryImpl.kt** → UserRemoteMediator.kt (Paging)
- **From UserRemoteMediator.kt** → ApiService.kt + UserDao.kt (Data Sources)

---

This structure demonstrates:
✅ Clean Architecture
✅ MVI Pattern
✅ Dependency Injection (Hilt)
✅ Database (Room)
✅ Networking (Retrofit)
✅ Pagination (Paging 3)
✅ Modern UI (Jetpack Compose)
✅ Kotlin Coroutines & Flow
