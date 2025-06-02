package no.nav.pensjon.integrationtest

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

@TestConfiguration
class DataSourceTestConfig {

    @Bean("DataSource")
    fun createTestDataSource(): DataSource {
        return DataSourceBuilder.create()
            .driverClassName("org.h2.Driver")
            .url("jdbc:h2:mem:mydbsporingslogg")
            .username("sa")
            .password("")
            .build()
    }

}