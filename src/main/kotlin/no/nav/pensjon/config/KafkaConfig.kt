package no.nav.pensjon.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import java.time.Duration
import javax.annotation.PostConstruct

@EnableKafka
@Profile("prod", "test")
@Configuration
class KafkaConfig(
    private val kafkaErrorHandler: KafkaErrorHandler?,
    @param:Value("\${SPRING.PROFILES.ACTIVE}") private val springProfile: String,
    @param:Value("\${kafka.keystore.path}") private val keystorePath: String,
    @param:Value("\${kafka.credstore.password}") private val credstorePassword: String,
    @param:Value("\${kafka.truststore.path}") private val truststorePath: String,
    @param:Value("\${kafka.brokers}") private val aivenBootstrapServers: String,
    @param:Value("\${ONPREM_KAFKA_BOOTSTRAP_SERVERS_URL}") private val onpremBootstrapServers: String,
    @param:Value("\${SRVUSERNAME}") private val srvusername: String,
    @param:Value("\${SRVPASSWORD}") private val srvpassword: String,
    @param:Value("\${kafka.sporingslogg.topic}") private val topic: String,
    @param:Value("\${kafka.sporingslogg.groupid}") private val groupid: String,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @PostConstruct
    fun initConfig() {
        val initconf = """
            *** KAFKA CONFIG ***
            *** Kafka topic: $topic
            *** Kafka group: $groupid
            *** Kafka broker: $onpremBootstrapServers
            *** Securitu Prot: ${onpremKafkaConsumerFactory().configurationProperties[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG]}
            *** Error handler: $kafkaErrorHandler
            ***************************************
        """
        log.debug(initconf)
    }

    fun aivenKafkaConsumerFactory(): ConsumerFactory<Int, String> {
        val configMap: MutableMap<String, Any> = HashMap()
        aivenCommonConfig(configMap)
        commonConfig(aivenBootstrapServers, configMap)

        return DefaultKafkaConsumerFactory(configMap, IntegerDeserializer(), StringDeserializer())
    }

    @Bean
    fun aivenKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<Int, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<Int, String>()
        factory.consumerFactory = aivenKafkaConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.containerProperties.setAuthExceptionRetryInterval( Duration.ofSeconds(4L))
//        if (kafkaErrorHandler != null) {
//            factory.setErrorHandler(kafkaErrorHandler)
//        }
        return factory
    }

    fun onpremKafkaConsumerFactory(): ConsumerFactory<Int, String> {
        val configMap: MutableMap<String, Any> = HashMap()
        onpremCommonConfig(configMap)
        commonConfig(onpremBootstrapServers, configMap)

        return DefaultKafkaConsumerFactory(configMap, IntegerDeserializer(), StringDeserializer())
    }

    @Bean
    fun onpremKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<Int, String>? {
        val factory = ConcurrentKafkaListenerContainerFactory<Int, String>()
        factory.consumerFactory = onpremKafkaConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.containerProperties.setAuthExceptionRetryInterval( Duration.ofSeconds(4L) )
//        if (kafkaErrorHandler != null) {
//            factory.setErrorHandler(kafkaErrorHandler)
//        }
        return factory
    }

    private fun commonConfig(bootstrapServer: String, configMap: MutableMap<String, Any>) {
        configMap[ConsumerConfig.CLIENT_ID_CONFIG] = groupid
        configMap[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServer
        configMap[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        configMap[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        configMap[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 1
    }


    private fun aivenCommonConfig(configMap: MutableMap<String, Any>) {
        configMap[SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG] = keystorePath
        configMap[SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG] = credstorePassword
        configMap[SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG] = credstorePassword
        configMap[SslConfigs.SSL_KEY_PASSWORD_CONFIG] = credstorePassword
        configMap[SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG] = "JKS"
        configMap[SslConfigs.SSL_KEYSTORE_TYPE_CONFIG] = "PKCS12"
        configMap[SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG] = truststorePath
        configMap[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = "SSL" // securityProtocol
    }

    private fun onpremCommonConfig(configMap: MutableMap<String, Any>) {
        configMap[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = SecurityProtocol.SASL_SSL.name
        configMap[SaslConfigs.SASL_MECHANISM] = "PLAIN"
        configMap[SaslConfigs.SASL_JAAS_CONFIG] = "org.apache.kafka.common.security.plain.PlainLoginModule required username='${srvusername}' password='${srvpassword}';"
    }

}