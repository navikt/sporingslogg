package no.nav.pensjon.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import javax.sql.DataSource


@Configuration
@Profile("!unsecured-webmvctest")
class DataSourceConfig(
    @param:Value("\${DBURL}") private val dburl: String,
    @param:Value("\${DBUSERNAME}") private val dbusername: String,
    @param:Value("\${DBPASSWORD}") private val dbpassword: String
    ) {

    @Bean("DataSource")
    fun createDataSource(): DataSource {
        val dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.url(dburl)
        dataSourceBuilder.username(dbusername)
        dataSourceBuilder.password(dbpassword)
        return dataSourceBuilder.build()
    }

}