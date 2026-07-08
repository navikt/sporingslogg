# Copilot Instructions for sporingslogg

## About this project
Kotlin Spring Boot service for **Team Pensjon Samhandling** at NAV. It receives audit log entries (who accessed whose data) from other services, stores them in Oracle DB, and exposes them to citizens via REST. Messages arrive either via Kafka or direct REST POST.

## Build, test, and run commands

```bash
# Build
./gradlew build

# Run all tests
./gradlew test

# Run a single test class
./gradlew test --tests "no.nav.pensjon.integrationtest.controller.PostControllerTest"

# Run a single test method
./gradlew test --tests "no.nav.pensjon.integrationtest.controller.PostControllerTest.myTestMethod"

# Build without tests
./gradlew build -x test
```

## Architecture

```
Kafka (Aiven) ──► KafkaLoggMeldingConsumer
                       │
REST POST ──────────► LoggTjeneste ──► LoggRepository ──► Oracle DB (SPORINGS_LOGG)
                                                               │
REST GET (citizen) ◄──────────────────────────────────────────┘
```

- **`listener/KafkaLoggMeldingConsumer`** — Consumes Aiven Kafka topic, validates, Base64-encodes `leverteData`, persists. Silently ACKs on deserialize/validation failure; retries on DB failure.
- **`controller/PostController`** — REST endpoint for system-to-system log submissions (`POST /sporingslogg/api/post`).
- **`controller/LesController`** — Citizen-facing read endpoint (`GET /api/les`), requires TokenDings with ACR Level4; returns only entries that have a `samtykkeToken`.
- **`controller/FinnController`** — Non-production search/query endpoints, requires SERVICEBRUKER token.
- **`tjeneste/LoggTjeneste`** — Service layer coordinating validation and persistence.
- **`tjeneste/ValideringTjeneste`** — Validates field constraints (person=11 digits, mottaker/leverandor=9 digits, tema=3 chars, field size limits).
- **`domain/LoggInnslag`** — JPA entity for table `SPORINGS_LOGG`, uses sequence `SPORINGS_LOGG_ID_SEQ`.

## Authentication

Four JWT issuers are supported (configured in `SecurityConfiguration` via `@EnableJwtTokenValidation`):

| Issuer key | Use | Token claim of interest |
|---|---|---|
| `servicebruker` | System accounts (STS) | `sub` |
| `tokendings` | Citizens (requires `acr=Level4`) | `pid` |
| `entraid` | Azure AD service accounts | Azure subject |
| `difi` | Legacy citizen token | `pid` |

`TokenHelper` centralises claim extraction. Use `getSystemUserOrEntraId()` for system identity, `getPidFromToken()` for citizen identity.

Endpoints use `@Protected` (any valid token) or `@ProtectedWithClaims(issuer = "tokendings", claimMap = ["acr=Level4"])`.

## Key conventions

- **Base64 encoding**: `leverteData` is always Base64-encoded before persistence. Do not store raw JSON.
- **PII in logs**: Use `Utils` scrambling helpers — never log raw person IDs or sensitive payload data.
- **Kafka error handling**: Deserialization and validation errors are swallowed and ACKed. Only DB errors should throw (triggering retry). Do not change this contract.
- **MDC context**: Kafka consumer sets `x_request_id` (UUID) and `tema` on MDC for each message. Controllers propagate via `CorrelasionFilter`.
- **Norwegian identifiers**: `person` is a Norwegian fødselsnummer/d-nummer (11 digits); `mottaker`/`leverandor` are organization numbers (9 digits).

## Test conventions

- All integration tests extend `BaseTests` (abstract), which starts `@EnableMockOAuth2Server` + `@EmbeddedKafka` and uses H2 in-memory DB.
- Token factories in `BaseTests`: `mockTokenDings(subject)`, `mockServiceToken()`, `mockEntraIdToken()`.
- Test data factories in `TestHelper`: `mockLoggMelding()`, `mockLoggInnslag()`, `mockLoggMeldingAsJson()`.
- Use `MockMvc` for controller tests. Kafka tests use `EmbeddedKafka` and poll the DB to assert results.
- Tests use **MockK** (not Mockito): `every { ... }`, `verify { ... }`, `mockk<T>()`.
