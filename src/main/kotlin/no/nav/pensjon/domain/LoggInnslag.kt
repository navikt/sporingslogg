package no.nav.pensjon.domain

import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Lob
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import no.nav.pensjon.util.scrable
import java.time.LocalDateTime


@Entity
@Table(name = "SPORINGS_LOGG")
open class LoggInnslag(
    // Logger en uthenting av data om en person
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @SequenceGenerator(name = "sporingsLoggIdGenerator", sequenceName = "SPORINGS_LOGG_ID_SEQ", allocationSize = 1, initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sporingsLoggIdGenerator")
    var id: Long? = null,

    @Column(name = "PERSON", nullable = false)
    var person: String? = null,

    @Column(name = "MOTTAKER", nullable = false)
    var mottaker: String? = null,

    @Column(name = "TEMA", nullable = false)
    var tema: String? = null,

    @Column(name = "HJEMMEL", nullable = false)
    var hjemmel: String? = null,

    @Column(name = "TIDSPUNKT", nullable = false)
    @Convert(converter = TimestampConverter::class)
    var uthentingsTidspunkt: LocalDateTime? = null,

    @Lob
    @Column(name = "LEVERTE_DATA", nullable = false)
    var leverteData: String? = null, // dataene som ble hentet ut, B64-encodet JSON-format

    @Column(name = "SAMTYKKE_TOKEN", nullable = true)
    var samtykkeToken: String? = null,  // JWT hvis hjemmel er 'samtykke'

    @Lob
    @Column(name = "FORESPORSEL", nullable = true)
    var foresporsel: String? = null, // Noen trenger å lagre requesten som ble brukt til å hente data som ble levert ut

    @Column(name = "LEVERANDOR", nullable = true)
    var leverandor: String? = null // Noen trenger å registrere at mottaker (orgnr) ikke er den som man har avtale med

    )  {

    override fun toString(): String {
        return "LoggInnslag [id=$id, person=${person.scrable()}, mottaker=$mottaker, tema=$tema, hjemmel=$hjemmel, " +
                "uthentingsTidspunkt=$uthentingsTidspunkt, leverteData=$leverteData, samtykkeToken=$samtykkeToken, dataForespoersel=$foresporsel, " +
                "leverandoer=$leverandor]"
    }

    companion object {
        fun fromLoggMelding(loggMelding: LoggMelding): LoggInnslag {
            return LoggInnslag(
                id = null,
                person = loggMelding.person,
                mottaker =loggMelding.mottaker,
                tema = loggMelding.tema,
                hjemmel = loggMelding.behandlingsGrunnlag,
                uthentingsTidspunkt = loggMelding.uthentingsTidspunkt,
                leverteData = loggMelding.leverteData,
                samtykkeToken = loggMelding.samtykkeToken,
                foresporsel = loggMelding.dataForespoersel,
                leverandor = loggMelding.leverandoer
            )
        }
    }

}