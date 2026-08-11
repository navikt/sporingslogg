package no.nav.pensjon.scheduling

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
// The bean only initializes if "app.db-monitor.enabled" is explicitly "true" in your environment
@ConditionalOnProperty(name = ["app.db-monitor.enabled"], havingValue = "true")
class DBSizeMonitor(
    private val jdbcTemplate: JdbcTemplate,
    meterRegistry: MeterRegistry
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Volatile
    private var databaseSizeMb: Double = 0.0

    init {
        Gauge.builder("sporingslogg.dbsize", this) { it.databaseSizeMb }
            .strongReference(true)
            .register(meterRegistry)
    }

    // Runs once a week on Mondays at 3:00 AM
    @Scheduled(cron = "0 0 3 * * MON")
    fun monitorOracleSize() {
        try {
            val sql = "SELECT SUM(bytes) FROM user_segments WHERE segment_name = 'SPORINGS_LOGG'"
            val bytes = jdbcTemplate.queryForObject(sql, BigDecimal::class.java)

            if (bytes != null) {
                databaseSizeMb = bytes.toDouble() / 1048576.0
                log.info("Gauge updated: Oracle Schema Used Space: {} MB", databaseSizeMb)
            }
        } catch (e: Exception) {
            log.warn("Failed to update Oracle metric: ${e.message}", e)
        }
    }
}