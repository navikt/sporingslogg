package no.nav.pensjon.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import java.time.Duration

@EnableKafka
@Profile("prod", "test")
@Configuration
class KafkaConfig(
    @param:Value("\${kafka.keystore.path}") private val keystorePath: String,
    @param:Value("\${kafka.credstore.password}") private val credstorePassword: String,
    @param:Value("\${kafka.truststore.path}") private val truststorePath: String,
    @param:Value("\${kafka.brokers}") private val aivenBootstrapServers: String,
    @param:Value("\${kafka.security.protocol}") private val securityProtocol: String,
    @param:Value("\${kafka.sporingslogg.aivengroupid}") private val aivenGroupid: String,
) {

    fun aivenKafkaConsumerFactory(): ConsumerFactory<Int, String> {
        val configMap: MutableMap<String, Any> = HashMap()
        aivenCommonConfig(configMap)
        commonConfig(aivenBootstrapServers, aivenGroupid, configMap)
        errorHandlingConfig(configMap)


        return DefaultKafkaConsumerFactory(configMap)
    }


    @Bean
    fun aivenKafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<Int, String> {
        val factory = ConcurrentKafkaListenerContainerFactory<Int, String>()
        factory.consumerFactory = aivenKafkaConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(4L))
        return factory
    }

    private fun commonConfig(bootstrapServer: String, groupid: String, configMap: MutableMap<String, Any>) {
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
        configMap[CommonClientConfigs.SECURITY_PROTOCOL_CONFIG] = securityProtocol
    }

    private fun errorHandlingConfig(configMap: MutableMap<String, Any>) {
        configMap[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        configMap[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = ErrorHandlingDeserializer::class.java
        configMap[ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS] = IntegerDeserializer::class.java
        configMap[ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS] = StringDeserializer::class.java
    }
}