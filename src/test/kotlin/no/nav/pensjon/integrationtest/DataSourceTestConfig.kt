package no.nav.pensjon.integrationtest

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

@TestConfiguration
class DataSourceTestConfig {

    @Bean("DataSource")
    fun createTestDataSource(): DataSource {
        //log.debug("Configure Testing DataSource: $dburl usr: $dbusername")
        val dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.driverClassName("org.h2.Driver")
        dataSourceBuilder.url("jdbc:h2:mem:mydbsporingslogg")
        dataSourceBuilder.username("sa")
        dataSourceBuilder.password("")
        return dataSourceBuilder.build()
    }

}