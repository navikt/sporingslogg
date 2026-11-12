package no.nav.pensjon.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.tjeneste.LoggRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StatistikkMetrikkerTest {
    private val repository = mockk<LoggRepository>()
    private val registry = SimpleMeterRegistry()
    private val metrikker = StatistikkMetrikker(repository, registry)

    private fun verdi(tema: String, samtykke: String) =
        registry.get("sporingslogg_loggInnslag_antall").tags("tema", tema, "samtykke", samtykke).gauge().value()

    @Test
    fun `teller per tema med og uten samtykke`() {
        every { repository.tellPerTema() } returns listOf(arrayOf("PEK", 10L, 4L), arrayOf("PEN", 3L, 0L))
        metrikker.oppdater()

        assertEquals(10.0, verdi("PEK", "alle"))
        assertEquals(4.0, verdi("PEK", "med_samtykke"))
        assertEquals(6.0, verdi("PEK", "uten_samtykke"))
        assertEquals(3.0, verdi("PEN", "uten_samtykke"))
    }

    @Test
    fun `tema som forsvinner nullstilles og feil beholder forrige verdi`() {
        every { repository.tellPerTema() } returns listOf(arrayOf("PEK", 5L, 1L))
        metrikker.oppdater()
        every { repository.tellPerTema() } returns emptyList()
        metrikker.oppdater()
        assertEquals(0.0, verdi("PEK", "alle"))

        every { repository.tellPerTema() } returns listOf(arrayOf("PEK", 7L, 2L))
        metrikker.oppdater()
        every { repository.tellPerTema() } throws RuntimeException("db nede")
        metrikker.oppdater()
        assertEquals(7.0, verdi("PEK", "alle"))
    }
}
