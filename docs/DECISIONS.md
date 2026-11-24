# Architecture & Technical Decisions

1. **RemoteMediator vs Custom PagingSource**:
   - Selected `RemoteMediator` combined with Room to guarantee true offline capability. Feed data is cached locally and displayed seamlessly even without network.

2. **Kotlinx Serialization vs Moshi/Gson**:
   - Selected `kotlinx.serialization` for native Kotlin compiler plugin performance, reflection-free parsing, and smaller binary overhead.

3. **In-Memory Robolectric Tests**:
   - Allowed DAO and Compose UI tests to run without requiring a physical device or emulator, enabling fast CI feedback within GitHub Actions.

4. **SavedStateHandle Integration**:
   - Integrated `SavedStateHandle` in `FeedViewModel`, `SearchViewModel`, and `SellViewModel` to guarantee state restoration on configuration changes and process death.
