# Plan: Swaply - Production-Quality Android Marketplace App

## Architecture
- **Clean Architecture + MVVM + MVI-style UDF**:
  - `data/`: Remote API (Retrofit/DummyJSON), Local DB (Room), Paging (RemoteMediator/PagingSource), Repositories
  - `domain/`: Models, Repository interfaces, Use cases
  - `ui/`: Compose Material 3 screens, ViewModels (SavedStateHandle, StateFlow), Navigation
  - `core/`: Dispatchers, Network monitor, Result/Resource types, Design tokens

## Milestones
1. **M1**: Project structure, Version catalog (`libs.versions.toml`), Gradle wrapper, Hilt, Theme, Navigation shell.
2. **M2**: Network layer (DummyJSON), Room (Product, Favorite, SearchQuery entities), ProductRepository, Paging 3.
3. **M3**: Home feed UI with categories, pull-to-refresh, empty/error/loading states.
4. **M4**: Search with 300ms debounce, recent search history.
5. **M5**: Product detail screen with carousel, live favorite toggling.
6. **M6**: Sell flow with validation, draft persistence, fake publish API.
7. **M7**: Offline banner, TalkBack accessibility, process-death handling.
8. **M8**: Performance optimizations, metrics, strict mode, R8 config.
9. **M9**: Complete CI workflow, documentation, test suite, and release APK configuration.
