# AI-Assisted Development Workflow

## Tooling & Architecture
This application was developed with AI-assisted pair-programming following production Android standards.

## Prompts & Interactions
1. **Scaffolding and Architectural Guidance**:
   - AI prompted to scaffold a multi-layer clean architecture with Room as the single source of truth and Paging 3.
2. **Review & Corrections Applied**:
   - Ensured `libs.versions.toml` correctly referenced stable Android Gradle Plugin 8.3.x, Kotlin 1.9.23, and Room 2.6.x.
   - Refined `ProductRemoteMediator` to correctly handle `LoadType.REFRESH` and `APPEND` without duplicate key errors in Room.
   - Verified that all Compose UI elements have `Modifier.testTag` and `contentDescription` for accessibility and TalkBack compatibility.
   - Added Robolectric Compose testing rules to ensure both Unit and UI tests run in fast JVM test runners.
