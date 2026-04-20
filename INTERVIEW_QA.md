# 🎤 Android Interview Q&A - Common Questions

## Architecture Questions

### Q1: Why Clean Architecture over traditional MVVM?

**Answer:**
"Clean Architecture provides better separation of concerns. In traditional MVVM, the ViewModel often depends directly on Android framework classes like Room or Retrofit. With Clean Architecture:

1. **Domain layer is pure Kotlin** - No Android dependencies, making it highly testable
2. **Business logic is centralized** - Use cases contain reusable business rules
3. **Data sources are swappable** - I can switch from Room to another database without touching domain
4. **Better for large teams** - Clear boundaries prevent developers from mixing concerns

However, for small apps, MVVM without Clean Architecture is perfectly fine. I use Clean Architecture when the app has complex business logic or a large team."

---

### Q2: Explain MVI vs MVVM

**Answer:**
"Both are architectural patterns, but they handle state differently:

**MVVM:**
- Multiple LiveData/StateFlows in ViewModel
- Two-way data binding possible
- State can be scattered across multiple observables

**MVI:**
- Single immutable state object
- Unidirectional data flow only
- Events go in, State comes out
- Effects handled separately via Channels

I prefer MVI because:
1. **Single source of truth** - UI is always a function of one state
2. **Easier debugging** - State changes are explicit
3. **Better testing** - State transitions are predictable
4. **No race conditions** - Immutable state prevents threading issues

Example:
```kotlin
// MVI
data class UiState(
    val users: List<User>,
    val isLoading: Boolean,
    val error: String?
)

// MVVM (scattered state)
val users: LiveData<List<User>>
val isLoading: LiveData<Boolean>
val error: LiveData<String>
```
"

---

### Q3: When would you use a UseCase vs calling Repository directly?

**Answer:**
"Use cases are optional middleware between ViewModel and Repository. I use them when:

**Use UseCase when:**
1. **Complex business logic** - Combining multiple repository calls
2. **Reusable operations** - Same logic needed in multiple ViewModels
3. **Business rules** - Validation, transformation, policy enforcement
4. **Future scalability** - Expecting business logic to grow

**Skip UseCase when:**
1. **Simple CRUD** - Just getting/saving data
2. **One-to-one mapping** - ViewModel method directly maps to Repository
3. **Small apps** - Extra abstraction not needed

Example needing UseCase:
```kotlin
class ProcessOrderUseCase(
    private val orderRepo: OrderRepository,
    private val paymentRepo: PaymentRepository,
    private val inventoryRepo: InventoryRepository
) {
    suspend operator fun invoke(order: Order): Result<Receipt> {
        // Complex multi-step business logic
        inventoryRepo.reserve(order.items)
        val payment = paymentRepo.process(order.total)
        val orderConfirm = orderRepo.create(order)
        return generateReceipt(payment, orderConfirm)
    }
}
```

For simple user list, calling repository directly is fine."

---

## Paging 3 Questions

### Q4: Explain how Paging 3 works with Room and Retrofit

**Answer:**
"Paging 3 uses a **RemoteMediator** to coordinate between network and database:

**Flow:**
```
User scrolls → Paging triggers load
                ↓
        RemoteMediator.load()
                ↓
    Check LoadType (REFRESH/APPEND)
                ↓
        Fetch from API
                ↓
        Save to Room
                ↓
    Room PagingSource emits data
                ↓
        UI updates
```

**Key components:**

1. **Pager** - Configures page size, prefetch distance
2. **RemoteMediator** - Handles network + cache
3. **PagingSource** - Comes from Room DAO automatically
4. **PagingData** - Holds paginated data

**Benefits:**
- **Offline-first** - Database is source of truth
- **Automatic pagination** - Library handles scrolling triggers
- **Memory efficient** - Only loads visible items + buffer
- **Built-in retry** - Error handling included

The RemoteMediator decides when to fetch:
- REFRESH: Initial load or pull-to-refresh → Clear and reload
- APPEND: Scrolling down → Load next page
- PREPEND: Scrolling up → Usually not needed for API pagination

```kotlin
@OptIn(ExperimentalPagingApi::class)
return Pager(
    config = PagingConfig(pageSize = 20),
    remoteMediator = UserRemoteMediator(api, db),
    pagingSourceFactory = { dao.getPagingSource() }
).flow
```
"

---

### Q5: How do you handle Paging 3 load states?

**Answer:**
"Paging 3 provides three LoadState types: **Loading**, **Error**, and **NotLoading**.

Each has three positions:
- **refresh** - Initial load
- **prepend** - Loading before first item
- **append** - Loading after last item

```kotlin
val lazyPagingItems = viewModel.users.collectAsLazyPagingItems()

// Initial loading
when (lazyPagingItems.loadState.refresh) {
    is LoadState.Loading -> ShowLoadingSpinner()
    is LoadState.Error -> {
        val error = (lazyPagingItems.loadState.refresh as LoadState.Error)
        ShowError(error.error.message)
    }
    is LoadState.NotLoading -> ShowContent()
}

// Pagination loading (bottom of list)
if (lazyPagingItems.loadState.append is LoadState.Loading) {
    ShowBottomLoadingIndicator()
}

// Pagination error
if (lazyPagingItems.loadState.append is LoadState.Error) {
    ShowRetryButton { lazyPagingItems.retry() }
}
```

**Best practices:**
1. Show initial loading fullscreen
2. Show append loading as footer in list
3. Provide retry buttons for errors
4. Handle empty state separately
"

---

## Dependency Injection Questions

### Q6: Why Hilt over Dagger or Manual DI?

**Answer:**
"Hilt is built on top of Dagger but reduces boilerplate significantly:

**Advantages over Dagger:**
1. **Less boilerplate** - No need to create components manually
2. **Android integration** - Built-in scopes for Activity, Fragment, ViewModel
3. **Lifecycle awareness** - Automatically cleans up when needed
4. **Jetpack integration** - Works seamlessly with ViewModel, WorkManager

**Advantages over Manual DI:**
1. **Compile-time safety** - Catches missing dependencies at build time
2. **No runtime reflection** - Better performance than Koin
3. **Automatic scoping** - Singletons, ViewModels handled correctly
4. **Graph validation** - Ensures all dependencies satisfied

**When I'd use alternatives:**
- **Koin** - Small app, prefer simplicity over compile-time safety
- **Manual DI** - Tiny app, learning project
- **Dagger** - Multi-module library where I need fine control

Example showing Hilt simplicity:
```kotlin
// Hilt - automatic ViewModel injection
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel()

@Composable
fun Screen(viewModel: MyViewModel = hiltViewModel())

// vs Manual Dagger requiring factory boilerplate
```
"

---

### Q7: Explain @Provides vs @Binds

**Answer:**
"Both are ways to tell Hilt how to provide dependencies, but they're used differently:

**@Provides** - For complex object creation:
```kotlin
@Provides
@Singleton
fun provideRetrofit(okHttp: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}
```
Use when:
- Creating instances with constructors
- Building complex objects
- Providing third-party classes

**@Binds** - For interface to implementation binding:
```kotlin
@Binds
@Singleton
abstract fun bindRepository(
    impl: RepositoryImpl
): Repository
```
Use when:
- Binding interface to implementation
- Implementation has @Inject constructor
- More efficient (generates less code)

**Key differences:**
- @Provides = concrete function in object module
- @Binds = abstract function in abstract module
- @Binds is more efficient at compilation

**Rule of thumb:** Use @Binds when possible, @Provides when necessary."

---

## Room Database Questions

### Q8: Why separate Entity from Domain Model?

**Answer:**
"Separation provides flexibility and clean architecture:

**Benefits:**

1. **Database schema independence**
   - Can change DB structure without affecting business logic
   - Add Room-specific annotations without polluting domain

2. **Clean domain layer**
   - Domain models are pure Kotlin, no Android dependencies
   - Easier to test, reuse, and reason about

3. **API vs Database differences**
   - API might return different fields than we store
   - Can map differently for each data source

4. **Flexibility for migrations**
   - Can change Entity without breaking domain contracts
   - Multiple entities can map to one domain model

**Example:**
```kotlin
// Domain (pure Kotlin)
data class User(
    val id: Int,
    val fullName: String,
    val email: String
)

// Room Entity (Android-specific)
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "full_name") val fullName: String,
    val email: String,
    @ColumnInfo(name = "cached_at") val cachedAt: Long
)

// Mapper
fun UserEntity.toDomain() = User(id, fullName, email)
```

**When to skip:** Very small apps where simplicity > architecture purity."

---

### Q9: How would you handle database migrations in Room?

**Answer:**
"Room supports automatic and manual migrations:

**Development:**
```kotlin
Room.databaseBuilder()
    .fallbackToDestructiveMigration() // Destroys data
    .build()
```

**Production:**
```kotlin
// Define migration
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            \"\"\"
            ALTER TABLE users 
            ADD COLUMN age INTEGER NOT NULL DEFAULT 0
            \"\"\".trimIndent()
        )
    }
}

// Add to builder
Room.databaseBuilder()
    .addMigrations(MIGRATION_1_2)
    .build()
```

**Best practices:**
1. **Test migrations** - Room provides testing utilities
2. **Keep migrations** - Don't delete old migration code
3. **Version carefully** - Increment version for each schema change
4. **Export schema** - exportSchema = true in @Database

**Complex scenario:**
```kotlin
// Testing migration
@Test
fun migrate1To2() {
    val db = helper.createDatabase(TEST_DB, 1)
    // Insert test data
    
    helper.runMigrationsAndValidate(TEST_DB, 2, true, MIGRATION_1_2)
    
    // Verify data intact
}
```
"

---

## Compose Questions

### Q10: Explain recomposition and how to optimize it

**Answer:**
"Recomposition is when Compose re-runs a composable function because state changed.

**How it works:**
1. State changes (remember, viewModel state, etc.)
2. Compose re-runs affected composables
3. Only changed parts of UI update

**Optimization strategies:**

1. **Stable inputs** - Use immutable data classes
```kotlin
// ❌ Recomposes unnecessarily
class User(var name: String)

// ✅ Stable, recomposes only when actually changed
data class User(val name: String)
```

2. **Remember expensive calculations**
```kotlin
// ❌ Recalculated every recomposition
val filtered = users.filter { it.active }

// ✅ Only recalculated when users changes
val filtered = remember(users) { 
    users.filter { it.active } 
}
```

3. **derivedStateOf for derived values**
```kotlin
val listState = rememberLazyListState()
val isAtTop by remember {
    derivedStateOf { listState.firstVisibleItemIndex == 0 }
}
```

4. **Key in lists**
```kotlin
// ✅ Compose knows what changed
items(
    items = users,
    key = { it.id }
) { user -> }
```

5. **Split composables** - Small, focused functions recompose less
```kotlin
// ❌ Entire screen recomposes
@Composable
fun Screen(users: List<User>, count: Int) {
    Column {
        Text("Count: $count") // Changes often
        UserList(users) // Doesn't change
    }
}

// ✅ Only Counter recomposes when count changes
@Composable
fun Screen(users: List<User>, count: Int) {
    Column {
        Counter(count)
        UserList(users)
    }
}
```

**Tools to find issues:**
- Layout Inspector
- Recomposition counts in logs
- Composition tracing
"

---

### Q11: When to use LaunchedEffect vs rememberCoroutineScope?

**Answer:**
"Both run coroutines, but they're used differently:

**LaunchedEffect:**
- Runs when entering composition
- Cancels when leaving composition
- **Restarts when key changes**

```kotlin
LaunchedEffect(userId) {
    // Restarts when userId changes
    viewModel.loadUser(userId)
}

LaunchedEffect(Unit) {
    // Runs once
    viewModel.effect.collect { /* handle */ }
}
```

Use for:
- One-time setup
- Collecting flows
- Side effects tied to lifecycle

**rememberCoroutineScope:**
- Returns CoroutineScope
- Tied to composition lifecycle
- Use for **event-triggered** work

```kotlin
val scope = rememberCoroutineScope()

Button(onClick = {
    scope.launch {
        // Triggered by click, not composition
        doSomething()
    }
})
```

Use for:
- Button clicks
- User interactions
- Manual coroutine launching

**Rule of thumb:**
- LaunchedEffect = automatic on composition/key change
- rememberCoroutineScope = manual trigger from events
"

---

## Coroutines Questions

### Q12: Explain StateFlow vs SharedFlow vs Flow

**Answer:**

**Flow (Cold):**
- Starts when collected
- Each collector gets its own stream
- Restarts for each collector

```kotlin
fun getUsers(): Flow<List<User>> = flow {
    // Executes for each collector
    val users = api.getUsers()
    emit(users)
}
```

**StateFlow (Hot):**
- Always active
- **Always has current value**
- Conflates (skips old values if not collected fast enough)
- Multiple collectors share same stream

```kotlin
private val _state = MutableStateFlow(UiState())
val state: StateFlow<UiState> = _state.asStateFlow()

// In UI
val state by viewModel.state.collectAsState() // Always has latest value
```

**SharedFlow (Hot):**
- Always active
- **No initial value** required
- Can replay values
- Multiple collectors

```kotlin
private val _events = MutableSharedFlow<Event>(
    replay = 0, // Don't replay
    extraBufferCapacity = 1
)
val events = _events.asSharedFlow()

// In UI
LaunchedEffect(Unit) {
    viewModel.events.collect { event ->
        // One-time events
    }
}
```

**When to use:**
- **Flow** - Repository operations, one-time requests
- **StateFlow** - UI state, always need current value
- **SharedFlow** - Events, navigation, no need for current value

**Example:**
```kotlin
class ViewModel {
    // State - UI needs current value always
    val state: StateFlow<UiState>
    
    // Events - One-time navigation, toasts
    val events: SharedFlow<UiEvent>
    
    // Data - Repository operation
    fun getUsers(): Flow<List<User>>
}
```
"

---

## Configuration Changes

### Q13: How do you handle configuration changes?

**Answer:**
"Android destroys and recreates Activities on configuration changes (rotation, language change). Here's how different components survive:

**ViewModel:**
- ✅ **Survives** - Core benefit of ViewModel
- State persists through rotation
- viewModelScope continues running

```kotlin
@HiltViewModel
class MyViewModel @Inject constructor() : ViewModel() {
    val state = MutableStateFlow(UiState())
    // Survives rotation automatically
}
```

**Paging 3:**
```kotlin
val users = repository.getUsers()
    .cachedIn(viewModelScope) // Caches across rotations
```

**Compose State:**
- remember = ❌ Lost on rotation
- rememberSaveable = ✅ Survives (if Parcelable/primitive)

```kotlin
// ❌ Lost
var text by remember { mutableStateOf("") }

// ✅ Survives
var text by rememberSaveable { mutableStateOf("") }
```

**SavedStateHandle:**
For complex non-Parcelable state:
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    var selectedId: Int
        get() = savedStateHandle["selected_id"] ?: 0
        set(value) { savedStateHandle["selected_id"] = value }
}
```

**Best practices:**
1. Put UI state in ViewModel (survives rotation)
2. Use rememberSaveable for simple UI state
3. Use SavedStateHandle for process death
4. Avoid storing state in Activity/Fragment
"

---

## Testing Questions

### Q14: How would you test this architecture?

**Answer:**
"Each layer needs different testing strategies:

**1. Domain Layer (Unit Tests):**
Easiest to test - pure Kotlin, no dependencies

```kotlin
class GetUsersUseCaseTest {
    @Test
    fun `returns users from repository`() = runTest {
        // Given
        val fakeRepo = FakeUserRepository()
        val useCase = GetUsersUseCase(fakeRepo)
        
        // When
        val result = useCase().first()
        
        // Then
        assertTrue(result is PagingData<User>)
    }
}
```

**2. ViewModel (Unit Tests):**
Test state changes and business logic

```kotlin
class UserViewModelTest {
    private lateinit var viewModel: UserViewModel
    private lateinit var fakeUseCase: GetUsersUseCase
    
    @Before
    fun setup() {
        fakeUseCase = FakeGetUsersUseCase()
        viewModel = UserViewModel(fakeUseCase)
    }
    
    @Test
    fun `refresh event updates state`() = runTest {
        // When
        viewModel.onEvent(UserEvent.Refresh)
        
        // Then
        assertNotNull(viewModel.state.value.users)
    }
}
```

**3. Repository (Unit Tests with Mocks):**
Mock API and DAO

```kotlin
class UserRepositoryTest {
    @Mock lateinit var api: ApiService
    @Mock lateinit var dao: UserDao
    
    @Test
    fun `getUserById returns cached first`() = runTest {
        // Given
        coEvery { dao.getUserById(1) } returns cachedUser
        val repo = UserRepositoryImpl(api, dao)
        
        // When
        val result = repo.getUserById(1)
        
        // Then
        assertEquals(cachedUser.toDomain(), result.getOrNull())
        coVerify(exactly = 0) { api.getUserById(1) }
    }
}
```

**4. UI (Instrumented Tests):**
Test composables

```kotlin
@Test
fun userList_displaysUsers() {
    composeTestRule.setContent {
        UserListScreen()
    }
    
    composeTestRule
        .onNodeWithText("John Doe")
        .assertExists()
}
```

**Test Doubles:**
- **Fake** - Working implementation (FakeRepository)
- **Mock** - Verify interactions (MockK)
- **Stub** - Return canned responses
"

---

Good luck with your interview! 🚀
