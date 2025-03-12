
# Team Samhandling Springslogg

[![Bygg alle brancher](https://github.com/navikt/sporingslogg/actions/workflows/bygg_alle_brancher.yml/badge.svg)](https://github.com/navikt/sporingslogg/actions/workflows/bygg_alle_brancher.yml)
[![Build](https://github.com/navikt/sporingslogg/workflows/Bygg%20og%20deploy/badge.svg)](https://github.com/navikt/sporingslogg/workflows/Bygg%20og%20deploy)

### Er en Kotlin Spring Boot applikasjon for team Pensjonsamhandling.

```
Tar imot LoggMeldinger (hendelser, restkall) fra tjenester som har hatt innsyn i brukeres historie.
Disse meldinger skal lagres og gjøres tilgjengelig for brukere når de logger inn i Nav.
```

* Lytter på kafka strøm og lagrer i db
* Rest tjeneste hvor systemer kan lagre i db (i stedet for kafka)

* Rest tjeneste hvor bruker kan hente opp sine data fra db
 - kun orginale hendelser hvor samtykke er med vil videre bli levert ut til nav.no

### Applikasjon sporingslogg vil fortsette å ACK meldinger selv om:
* Feiler ved konvertering fra Json hendelse (json er ikke korrekt se under hvordan melding skal se ut)
* Feiler ved validering av hendelsen påkrevde felt, størrelse på feil o.l 



### Eksempel på hvordan melding skal se ut:

```
{
"person": "12345678901",                           // Fnr/dnr for personen dataene gjelder
"mottaker": "123456789",                           // Orgnr som dataene leveres ut til Skal være 9 sifre
"tema": "ABC",                                     // Type data, som definert i https://modapp.adeo.no/kodeverksklient/viskodeverk???, Tema 3 tegn
"behandlingsGrunnlag": "hjemmelbeskrivelse",       // Beskriver hjemmel/samtykke som er bakgrunn for at dataene utleveres TODO kodeverk e.l. Max 100 tegn
"uthentingsTidspunkt": "2018-10-19T12:24:21.675",  // Tidspunkt for utlevering, ISO-format uten tidssone
"leverteData": "<Base64-encodet JSON-melding>",    // Utleverte data, max 1.000.000 tegn (i praksis må hele loggmeldingen være under Kafkas grense på 1 MB)
"samtykkeToken": "<JSON Web Token, encodet form>", // Samtykketoken produsert av Altinn, definert i https://altinn.github.io/docs/guides/samtykke/datakilde/bruk-av-token/ Max 1000 tegn
"dataForespoersel": "<forespørselen som er brukt>", // Request/dok hvordan NAV hentet data, max 100.000 tegn
"leverandoer": "123456789"                          // Orgnr til den som har utleveringsavtalen, benyttes ved delegering Skal være 9 sifre
}
```



### kontakt oss på NAV slack: 
#pensjon_samhandling
