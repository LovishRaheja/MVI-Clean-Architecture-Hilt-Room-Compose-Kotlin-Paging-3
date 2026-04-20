# Android Live Coding Interview - Complete Reference

This project demonstrates **MVI Clean Architecture** with modern Android stack: **Hilt, Room, Compose, Kotlin, and Paging 3**.

---

## 🏗️ Architecture Overview

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (UI, ViewModels, MVI State/Events)     │
│  ├─ UserListScreen.kt                   │
│  ├─ UserListViewModel.kt                │
│  └─ UserListContract.kt                 │
└─────────────────────────────────────────┘
            ↓ uses
┌─────────────────────────────────────────┐
│          Domain Layer                   │
│  (Business Logic, Use Cases, Models)    │
│  ├─ User.kt (entity)                    │
│  ├─ UserRepository.kt (interface)       │
│  └─ GetUsersUseCase.kt                  │
└─────────────────────────────────────────┘
            ↓ implements
┌─────────────────────────────────────────┐
│           Data Layer                    │
│  (Repositories, Data Sources, DTOs)     │
│  ├─ UserRepositoryImpl.kt               │
│  ├─ ApiService.kt (remote)              │
│  ├─ UserDao.kt (local)                  │
│  └─ UserRemoteMediator.kt (paging)      │
└─────────────────────────────────────────┘
```

### Dependency Rule
- **Presentation** depends on **Domain**
- **Data** depends on **Domain**
- **Domain** depends on NOTHING (pure Kotlin)

---

## 🎯 MVI Pattern Explained

### What is MVI?

**Model-View-Intent** is a unidirectional data flow pattern:

```
┌──────────┐
│   View   │ ──── Events ────▶ ┌──────────────┐
│ (Screen) │                   │  ViewModel   │
└──────────┘ ◀─── State ────── └──────────────┘
                                       │
                                   processes
                                       │
                                  ┌────▼────┐
                                  │  Model  │
                                  │ (State) │
                                  └─────────┘
```

### Key Components

1. **State** (`UserListState`)
   - Single source of truth
   - Immutable data class
   - Represents entire UI state
   - Exposed as `StateFlow`

2. **Events** (`UserListEvent`)
   - User actions/intentions
   - Sealed class for type safety
   - Examples: Refresh, Navigate, Click

3. **Effects** (`UserListEffect`)
   - One-time side effects
   - Different from persistent state
   - Examples: Navigation, Snackbar, Toast
   - Exposed as `Flow` from `Channel`

### Why MVI?

✅ **Predictable**: State changes are explicit
✅ **Testable**: Pure functions, easy to unit test
✅ **Debuggable**: Single state makes debugging easier
✅ **Thread-safe**: Immutable state prevents race conditions

---

## 🔧 Hilt Dependency Injection

### Setup Checklist

1. ✅ Add Hilt plugin to `build.gradle.kts`
2. ✅ Annotate Application class with `@HiltAndroidApp`
3. ✅ Annotate Activity with `@AndroidEntryPoint`
4. ✅ Create modules with `@Module` + `@InstallIn`

### Module Types

```kotlin
// Singleton-scoped dependencies
@InstallIn(SingletonComponent::class)

// Activity-scoped dependencies
@InstallIn(ActivityComponent::class)

// ViewModel-scoped dependencies
@InstallIn(ViewModelComponent::class)
```

### @Provides vs @Binds

**@Provides** - For complex instantiation:
```kotlin
@Provides
@Singleton
fun provideRetrofit(client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .build()
}
```

**@Binds** - For interface → implementation (more efficient):
```kotlin
@Binds
@Singleton
abstract fun bindUserRepository(
    impl: UserRepositoryImpl
): UserRepository
```

### ViewModel Injection

```kotlin
@HiltViewModel
class UserListViewModel @Inject constructor(
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel()
```

In Compose:
```kotlin
@Composable
fun UserListScreen(
    viewModel: UserListViewModel = hiltViewModel()
)
```

---

## 🗄️ Room Database

### Key Components

1. **Entity** - Database table
```kotlin
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String
)
```

2. **DAO** - Data Access Object
```kotlin
@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getUsersPagingSource(): PagingSource<Int, UserEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)
}
```

3. **Database** - Holds DAOs
```kotlin
@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
```

### Why Separate Entities from Domain Models?

✅ **Flexibility**: Database schema changes don't affect domain
✅ **Clean separation**: Domain is pure Kotlin, no Android deps
✅ **Testing**: Can test domain logic without Room

---

## 📄 Paging 3 Deep Dive

### Architecture Components

```
┌──────────────┐
│  PagingData  │ ◀─── Flow from Repository
└──────┬───────┘
       │
┌──────▼────────┐
│     Pager     │ ─── Configuration (page size, prefetch)
└──────┬────────┘
       │
┌──────▼─────────────┐
│ RemoteMediator     │ ─── Network + Cache coordination
│ (UserRemoteMediator)│
└──────┬─────────────┘
       │
┌──────▼────────┐
│ PagingSource  │ ─── From Room DAO
│ (Room)        │
└───────────────┘
```

### How It Works

1. **User scrolls** → Paging library triggers load
2. **RemoteMediator** checks if network fetch needed
3. **Fetch from API** if necessary
4. **Save to Room** database
5. **Room PagingSource** emits data to UI
6. **UI updates** automatically

### LoadType Explained

```kotlin
when (loadType) {
    LoadType.REFRESH -> // Initial load or pull-to-refresh
    LoadType.PREPEND -> // Load before first item (scroll up)
    LoadType.APPEND -> // Load after last item (scroll down)
}
```

### Load States

```kotlin
when (users.loadState.refresh) {
    is LoadState.Loading -> // Show loading indicator
    is LoadState.Error -> // Show error message
    is LoadState.NotLoading -> // Show content
}
```

### Key Benefits

✅ **Automatic pagination**: No manual page tracking
✅ **Memory efficient**: Only keeps data in viewport + buffer
✅ **Built-in retry**: Error handling included
✅ **Configuration changes**: Survives rotation with `cachedIn(viewModelScope)`

---

## 🎨 Jetpack Compose

### Key Concepts for Interview

**1. Composables are Functions**
```kotlin
@Composable
fun UserItem(user: User) {
    // Declarative UI
}
```

**2. State Management**
```kotlin
// Read state
val state by viewModel.state.collectAsState()

// Derive state
val isEmpty by remember { derivedStateOf { users.itemCount == 0 } }
```

**3. Side Effects**
```kotlin
// Run once
LaunchedEffect(Unit) {
    viewModel.effect.collect { /* handle */ }
}

// Run when key changes
LaunchedEffect(userId) {
    viewModel.loadUser(userId)
}
```

**4. Recomposition**
- Composables rerun when state changes
- Only affected parts recompose
- Use `remember` to survive recomposition

---

## 🚀 Interview Talking Points

### Why Clean Architecture?

> "Clean Architecture separates concerns into layers. The domain layer contains business logic with zero Android dependencies, making it testable and reusable. The data layer handles implementation details like network and database, while presentation handles UI. This makes the code maintainable, testable, and scalable."

### Why MVI over MVVM?

> "MVI provides unidirectional data flow which makes state management more predictable. Unlike MVVM where ViewModels can expose multiple LiveData/StateFlows, MVI has a single state object. This eliminates state synchronization issues and makes the UI a pure function of state."

### When to Use Use Cases?

> "Use cases are optional. I use them when there's complex business logic, when combining multiple repositories, or when the same operation is needed in multiple ViewModels. For simple CRUD operations, calling the repository directly is fine."

### How Does Paging 3 Work?

> "Paging 3 uses RemoteMediator to coordinate between network and database. It fetches from the API, saves to Room, and emits from Room's PagingSource. This creates an offline-first architecture where the database is the single source of truth."

### Why Hilt over Manual DI?

> "Hilt reduces boilerplate and provides compile-time safety. It automatically handles scoping, making sure singletons live as long as the app and ViewModels survive configuration changes. It also integrates seamlessly with Jetpack components."

---

## 🧪 Common Interview Questions

### Q: How do you handle configuration changes?

**A:** "ViewModels survive configuration changes, so state persists. For Paging, I use `cachedIn(viewModelScope)` which caches the PagingData. For Compose, `rememberSaveable` persists across process death."

### Q: How would you add offline support?

**A:** "Already built in! RemoteMediator saves API responses to Room. When offline, Room serves cached data. I'd add a `networkAvailable` check and show a UI indicator when in offline mode."

### Q: How do you test this architecture?

```kotlin
// Domain layer - Pure Kotlin, easy to test
@Test
fun `getUsersUseCase returns paging data`() = runTest {
    val repository = FakeUserRepository()
    val useCase = GetUsersUseCase(repository)
    
    val result = useCase().first()
    assertNotNull(result)
}

// ViewModel - Test state changes
@Test
fun `refresh updates state`() = runTest {
    val viewModel = UserListViewModel(fakeUseCase)
    
    viewModel.onEvent(UserListEvent.Refresh)
    
    val state = viewModel.state.value
    assertTrue(state.users != emptyFlow<PagingData<User>>())
}
```

### Q: How would you add search functionality?

**A:** "I'd add a search query to the State, pass it to RemoteMediator, and invalidate the PagingSource when the query changes. Room would filter results, and Paging would handle pagination of filtered data."

### Q: Explain your error handling strategy

**A:** "Paging 3 exposes LoadState for errors. I handle it at three levels:
1. UI layer - Show error message with retry button
2. Repository layer - Wrap calls in Result/try-catch
3. RemoteMediator - Return MediatorResult.Error for Paging to handle"

---

## 📦 Project Structure

```
com.example.interviewprep/
├── data/
│   ├── local/
│   │   ├── entity/UserEntity.kt
│   │   ├── dao/UserDao.kt
│   │   └── AppDatabase.kt
│   ├── remote/
│   │   ├── dto/UserDto.kt
│   │   └── ApiService.kt
│   ├── paging/
│   │   └── UserRemoteMediator.kt
│   └── repository/
│       └── UserRepositoryImpl.kt
├── domain/
│   ├── model/User.kt
│   ├── repository/UserRepository.kt
│   └── usecase/GetUsersUseCase.kt
├── presentation/
│   └── users/
│       ├── UserListContract.kt (State/Event/Effect)
│       ├── UserListViewModel.kt
│       └── UserListScreen.kt
├── di/
│   ├── NetworkModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
├── InterviewPrepApp.kt
└── MainActivity.kt
```

---

## 🎓 Quick Reference Cheatsheet

### Hilt Annotations
- `@HiltAndroidApp` - Application class
- `@AndroidEntryPoint` - Activity/Fragment
- `@HiltViewModel` - ViewModel
- `@Module` - Dependency module
- `@InstallIn` - Component scope
- `@Provides` - Provide dependency
- `@Binds` - Bind interface to implementation
- `@Singleton` - Single instance

### Room Annotations
- `@Database` - Database class
- `@Entity` - Table
- `@Dao` - Data Access Object
- `@PrimaryKey` - Primary key
- `@Query` - SQL query
- `@Insert` - Insert operation
- `@Update` - Update operation
- `@Delete` - Delete operation

### Compose Side Effects
- `LaunchedEffect` - Run suspend code, restart on key change
- `DisposableEffect` - Cleanup on leave composition
- `rememberCoroutineScope` - Get scope tied to composition
- `remember` - Survive recomposition
- `derivedStateOf` - Compute state from other state

### Paging 3 Key Classes
- `Pager` - Main entry point
- `PagingConfig` - Configuration (pageSize, prefetchDistance)
- `PagingSource` - Data source (from Room)
- `RemoteMediator` - Network + cache coordinator
- `PagingData` - Container for paged data
- `LazyPagingItems` - Compose integration

---

## 💡 Tips for Live Coding

1. **Start with domain layer** - Pure Kotlin, no dependencies
2. **Explain as you type** - Walk through your decisions
3. **Use meaningful names** - Code should be self-documenting
4. **Add comments** - Especially for complex logic
5. **Handle errors** - Don't ignore edge cases
6. **Think out loud** - Show your problem-solving process

---

## 🔥 Red Flags to Avoid

❌ God classes - Keep classes focused
❌ Business logic in UI - Move to ViewModel/UseCase
❌ Tight coupling - Use interfaces
❌ Ignoring lifecycle - Use lifecycle-aware components
❌ Memory leaks - Don't hold Activity references
❌ Main thread blocking - Use coroutines
❌ No error handling - Always handle failures

---

## ✅ Build & Run

```bash
# Clone project
git clone <repo>

# Open in Android Studio
# Wait for Gradle sync

# Run on emulator or device
./gradlew installDebug
```

**Note:** Update `BASE_URL` in `NetworkModule.kt` to match your API.

---

Good luck with your interview! 🚀
