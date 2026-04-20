# 🎯 Android Interview Quick Reference Cheat Sheet

## MVI Pattern

### State Flow
```kotlin
// ViewModel
private val _state = MutableStateFlow(UiState())
val state: StateFlow<UiState> = _state.asStateFlow()

// Update state immutably
_state.update { it.copy(isLoading = true) }

// Composable
val state by viewModel.state.collectAsState()
```

### Event Handling
```kotlin
sealed class UiEvent {
    data object Refresh : UiEvent()
    data class Navigate(val id: Int) : UiEvent()
}

// ViewModel
fun onEvent(event: UiEvent) {
    when (event) {
        is UiEvent.Refresh -> refresh()
        is UiEvent.Navigate -> navigate(event.id)
    }
}

// UI
onClick = { viewModel.onEvent(UiEvent.Navigate(id)) }
```

### Effects (One-time events)
```kotlin
// ViewModel
private val _effect = Channel<UiEffect>()
val effect = _effect.receiveAsFlow()

// Send effect
_effect.send(UiEffect.ShowToast("Success"))

// Composable
LaunchedEffect(Unit) {
    viewModel.effect.collect { effect ->
        when (effect) {
            is UiEffect.ShowToast -> // show toast
        }
    }
}
```

---

## Paging 3

### Basic Setup
```kotlin
// Repository
fun getItems(): Flow<PagingData<Item>> {
    return Pager(
        config = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false
        ),
        pagingSourceFactory = { dao.getPagingSource() }
    ).flow
}

// ViewModel
val items = repository.getItems()
    .cachedIn(viewModelScope)

// Composable
val items = viewModel.items.collectAsLazyPagingItems()
```

### RemoteMediator Pattern
```kotlin
@OptIn(ExperimentalPagingApi::class)
class MyRemoteMediator : RemoteMediator<Int, Entity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, Entity>
    ): MediatorResult {
        return try {
            val page = when (loadType) {
                LoadType.REFRESH -> 1
                LoadType.PREPEND -> return MediatorResult.Success(true)
                LoadType.APPEND -> calculateNextPage(state)
            }
            
            val data = api.getData(page)
            
            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    dao.clearAll()
                }
                dao.insertAll(data)
            }
            
            MediatorResult.Success(endOfPaginationReached = data.isEmpty())
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
```

### Load States in Compose
```kotlin
val items = viewModel.items.collectAsLazyPagingItems()

// Initial loading
if (items.loadState.refresh is LoadState.Loading) {
    CircularProgressIndicator()
}

// Error
if (items.loadState.refresh is LoadState.Error) {
    ErrorView(onRetry = { items.retry() })
}

// Append loading (bottom)
if (items.loadState.append is LoadState.Loading) {
    CircularProgressIndicator()
}

// LazyColumn
LazyColumn {
    items(
        count = items.itemCount,
        key = { items[it]?.id ?: it }
    ) { index ->
        items[index]?.let { item ->
            ItemView(item)
        }
    }
}
```

---

## Hilt Dependency Injection

### Setup
```kotlin
// build.gradle.kts
plugins {
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-android-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}

// Application
@HiltAndroidApp
class MyApp : Application()

// Activity
@AndroidEntryPoint
class MainActivity : ComponentActivity()

// ViewModel
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel()
```

### Modules
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindRepo(impl: RepoImpl): Repo
}
```

### Qualifiers
```kotlin
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Provides
@IoDispatcher
fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

class MyRepo @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher
)
```

---

## Room Database

### Complete Setup
```kotlin
// Entity
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey val id: Int,
    val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

// DAO
@Dao
interface ItemDao {
    @Query("SELECT * FROM items ORDER BY id ASC")
    fun getPagingSource(): PagingSource<Int, ItemEntity>
    
    @Query("SELECT * FROM items WHERE id = :id")
    suspend fun getById(id: Int): ItemEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(items: List<ItemEntity>)
    
    @Update
    suspend fun update(item: ItemEntity)
    
    @Delete
    suspend fun delete(item: ItemEntity)
    
    @Query("DELETE FROM items")
    suspend fun clearAll()
}

// Database
@Database(
    entities = [ItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

// Module
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
    return Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "app_database"
    ).fallbackToDestructiveMigration()
     .build()
}
```

---

## Jetpack Compose

### State Management
```kotlin
// Remember
var text by remember { mutableStateOf("") }

// Derive state
val isEmpty by remember { derivedStateOf { text.isEmpty() } }

// Stateful
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}

// Stateless (hoisting)
@Composable
fun Counter(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) {
        Text("Count: $count")
    }
}
```

### Side Effects
```kotlin
// LaunchedEffect - Restart on key change
LaunchedEffect(userId) {
    viewModel.loadUser(userId)
}

// DisposableEffect - Cleanup
DisposableEffect(Unit) {
    val listener = createListener()
    onDispose { listener.cleanup() }
}

// rememberCoroutineScope
val scope = rememberCoroutineScope()
Button(onClick = {
    scope.launch { doSomething() }
})
```

### Lists
```kotlin
LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(
        items = list,
        key = { it.id }
    ) { item ->
        ItemCard(item)
    }
    
    // Or with count
    items(
        count = list.size,
        key = { list[it].id }
    ) { index ->
        ItemCard(list[index])
    }
}
```

---

## Coroutines

### ViewModel Scope
```kotlin
class MyViewModel : ViewModel() {
    
    init {
        viewModelScope.launch {
            // Cancelled when ViewModel cleared
        }
    }
    
    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                val result = repository.getData()
                _state.update { it.copy(data = result, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
```

### Flows
```kotlin
// Cold flow
fun getItems(): Flow<List<Item>> = flow {
    val items = api.getItems()
    emit(items)
}

// Hot flow - StateFlow
private val _state = MutableStateFlow(State())
val state: StateFlow<State> = _state.asStateFlow()

// Hot flow - SharedFlow
private val _events = MutableSharedFlow<Event>()
val events: SharedFlow<Event> = _events.asSharedFlow()

// Collect in Composable
val state by viewModel.state.collectAsState()

// Collect in ViewModel
init {
    repository.getItems()
        .onEach { items ->
            _state.update { it.copy(items = items) }
        }
        .launchIn(viewModelScope)
}
```

---

## Retrofit

### Setup
```kotlin
// Interface
interface ApiService {
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int
    ): List<UserDto>
    
    @POST("users")
    suspend fun createUser(@Body user: UserDto): UserDto
    
    @PUT("users/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Body user: UserDto
    ): UserDto
    
    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Int)
}

// Module
@Provides
@Singleton
fun provideRetrofit(): Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    )
    .addConverterFactory(GsonConverterFactory.create())
    .build()

@Provides
@Singleton
fun provideApi(retrofit: Retrofit): ApiService =
    retrofit.create(ApiService::class.java)
```

---

## Testing Patterns

### ViewModel Test
```kotlin
@Test
fun `onRefresh updates loading state`() = runTest {
    val viewModel = MyViewModel(fakeRepository)
    
    viewModel.onEvent(Event.Refresh)
    
    assertEquals(true, viewModel.state.value.isLoading)
}
```

### Repository Test
```kotlin
@Test
fun `getUser returns cached data first`() = runTest {
    val repository = MyRepositoryImpl(api, dao)
    
    coEvery { dao.getUser(1) } returns cachedUser
    
    val result = repository.getUser(1)
    
    assertEquals(cachedUser.toDomain(), result.getOrNull())
    coVerify(exactly = 0) { api.getUser(1) }
}
```

---

## Common Patterns

### Resource Wrapper
```kotlin
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    data object Loading : Resource<Nothing>()
}

// Usage
suspend fun getData(): Resource<Data> {
    return try {
        val data = api.getData()
        Resource.Success(data)
    } catch (e: Exception) {
        Resource.Error(e.message ?: "Unknown error")
    }
}
```

### Result Type
```kotlin
suspend fun getUser(id: Int): Result<User> {
    return try {
        val user = api.getUser(id)
        Result.success(user)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Usage
val result = repository.getUser(1)
result.onSuccess { user -> }
result.onFailure { error -> }
```

---

## Interview Buzzwords to Use

✅ "Unidirectional data flow"
✅ "Single source of truth"
✅ "Separation of concerns"
✅ "Dependency inversion"
✅ "Lifecycle-aware components"
✅ "Reactive programming"
✅ "Immutable state"
✅ "Type-safe navigation"
✅ "Compile-time safety"
✅ "Configuration changes"

---

## Quick Wins in Interview

1. **Explain WHY** - Don't just write code, explain architectural decisions
2. **Error handling** - Always show you're thinking about edge cases
3. **Memory leaks** - Mention lifecycle awareness
4. **Thread safety** - Show understanding of coroutines/threading
5. **Testability** - Point out how your code is easy to test
6. **Scalability** - Discuss how the architecture grows with features

---

## Red Flags Interviewers Look For

❌ Business logic in UI layer
❌ Hardcoded strings/values
❌ No null safety
❌ Blocking main thread
❌ Memory leaks (Activity references)
❌ No error handling
❌ Tight coupling
❌ God classes
❌ No separation of concerns

---

This is your quick reference during live coding. Good luck! 🚀
