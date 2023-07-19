package no.nav.pensjon.tjeneste

import no.nav.pensjon.domain.LoggInnslag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LoggRepository: JpaRepository<LoggInnslag, Long> {

    @Query(
        value = "from LoggInnslag as l " +
                " where l.person = :personIdent"
    )
    fun hantAlleLoggInnslagForPerson(@Param("personIdent") personIdent: String): List<LoggInnslag>

    @Query(
        value = "SELECT count(l) from LoggInnslag as l " +
                " where l.person = :personIdent"
    )
    fun countAlleLoggInnslagForPerson(@Param("personIdent") personIdent: String): Int


    @Query(value = "from LoggInnslag as l where l.person like CONCAT(:personIdent, '%%')" )
    fun finnAllePersonStarterMed(@Param("personIdent") personIdent: String): List<LoggInnslag>

}