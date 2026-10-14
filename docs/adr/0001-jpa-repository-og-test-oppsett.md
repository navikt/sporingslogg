# ADR-0001: JPA-, Repository- og Test-oppsett i sporingslogg

| | |
|---|---|
| **Status** | Foreslått |
| **Dato** | 2026-08-31 |
| **Oppdatert** | 2026-08-31 |
| **Forfattere** | Pensjonsamhandling |

---

## Kontekst

sporingslogg har i dag et JPA/repository-lag (`LoggInnslag`, `LoggRepository`, `LoggTjeneste`) og et testoppsett delt i to:

- **Enhetstester** (`ValideringTjenesteTest`, `LoggMeldingValidatorTest`, `TokenHelperTest`, `UtilTest`, `KafkaLoggMeldingConsumerTest`) — kjører uten Spring-kontekst, raske, bruker MockK. Ligger i pakker som speiler `main`.
- **Integrasjonstester** (pakke `integrationtest`) — arver `BaseTests`, som starter full `@SpringBootTest`-kontekst med H2, `EmbeddedKafka` og `MockOAuth2Server`.

En gjennomgang av JPA- og testoppsettet avdekket flere forbedringspunkter, beskrevet under.

### Funn

| # | Område | Beskrivelse |
|---|--------|-------------|
| 1 | `LoggTjeneste.lagreLoggInnslag` | 8 linjer utkommentert valideringskode (`mottaker`, `tema`, `hjemmel`, `leverteData`, `samtykkeToken`, `foresporsel`, `leverandor`). Uklart om dette er forlatt duplisering av `LoggMeldingValidator`, som allerede validerer alt dette i controller-laget. |
| 2 | `LoggRepository` | Metodenavn-typo: `hantAlleLoggInnslagForPerson` skal være `hentAlleLoggInnslagForPerson`. |
| 3 | JPA-testdekning | Ingen `@DataJpaTest` — repository-spørringer (custom JPQL med `samtykkeToken IS NOT NULL`, `LIKE`-prefikssøk) testes kun indirekte via tunge full-kontekst-tester. |
| 4 | Testnavngivning | Integrasjonstester ligger i pakke `integrationtest`, men har ikke et konsekvent filnavn-suffiks (f.eks. `*IT.kt`) som skiller dem fra enhetstester ved verktøynivå. |
| 5 | Gradle-oppsett | Ingen egen source-set/task for integrasjonstester. `./gradlew test` kjører alt i én JVM/task, og `failFast = true` gjelder globalt — én treg/flaky integrasjonstest stopper også alle enhetstester i samme kjøring. |
| 6 | Testisolasjon | `BaseTests` deler én Spring-kontekst/H2-database på tvers av alle tester, uten `@Transactional` eller `@DirtiesContext`. Hver test må derfor velge unike fødselsnumre manuelt for å unngå datakollisjon mellom tester. |

---

## Beslutning

Rydd opp JPA-laget og innfør et tydelig skille mellom enhetstester og integrasjonstester, i to faser — grønn sone først, deretter rød sone som krever mer testing/diskusjon.

### Fase 1 — Grønn sone (lav risiko, kan gjøres uavhengig)

1. Fjern eller dokumenter dødt/utkommentert valideringskode i `LoggTjeneste.lagreLoggInnslag`
2. Rett typo `hantAlleLoggInnslagForPerson` → `hentAlleLoggInnslagForPerson`
3. Legg til `@DataJpaTest`-tester for `LoggRepository` for rask, isolert verifisering av custom-spørringene

### Fase 2 — Rød sone (krever mer risikovurdering)

4. Rename integrasjonstestfiler til `*IT.kt`-suffiks (f.eks. `PostControllerIT.kt`) for tydelig konvensjon
5. Splitt Gradle-testoppsettet: egen `integrationTest`-source-set/task, `failFast` kun på enhetstester
6. Innfør `@Transactional` (automatisk rollback per test) eller `@DirtiesContext` på `BaseTests` for ekte testisolasjon

---

## Konsekvenser

### Positivt

- Ryddigere og mer forutsigbar JPA-kode uten forvirrende dødt kode
- Raskere tilbakemelding lokalt — kan kjøre kun enhetstester under utvikling
- Tryggere tester — ingen skjult avhengighet av kjørerekkefølge eller delt databasetilstand
- Repository-spørringer testes presist og raskt med `@DataJpaTest`

### Negativt / risiko

- **Fase 2, punkt 6**: Å innføre `@Transactional`/rollback kan avdekke skjulte avhengigheter mellom eksisterende tester som i dag «tilfeldigvis» fungerer på grunn av delt state. Bør gjøres inkrementelt, ikke som én stor omskriving.
- **Fase 2, punkt 5**: Endrer byggetid og teststruktur. CI plukker i dag opp alle XML-rapporter under `build/test-results/**/*.xml` uavhengig av task-navn, så risikoen er lav, men bør verifiseres i CI før merge til `master`.
- Rename av testfiler (punkt 4) berører mange filer og import-referanser — bør gjøres i egen PR, adskilt fra funksjonelle endringer.

### Ikke berørt

- Eksisterende enhetstester (`ValideringTjenesteTest` m.fl.) er allerede godt strukturert og påvirkes ikke av denne ADR-en.

---

## Åpne spørsmål

1. Skal rename-konvensjonen være `*IT.kt` eller `*IntegrationTest.kt`? Begge brukes i Nav-økosystemet.
2. Skal Fase 2 gjennomføres i én PR eller splittes ytterligere (f.eks. rename separat fra Gradle-splitten)?
