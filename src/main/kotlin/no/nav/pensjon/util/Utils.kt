package no.nav.pensjon.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun String?.scrable() = if (this == null || this.length < 6 ) this else this.dropLast(5) + "xxxxx"
fun String?.trunc() = this?.take(250) + "...."

inline fun <reified T : Any> typeRefs(): TypeReference<T> = object : TypeReference<T>() {}

inline fun <reified T : Any> fromJson2Any(json: String, typeRef: TypeReference<T> = typeRefs(), failonunknown: Boolean = true): T {
    return jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failonunknown)
            .registerModule(JavaTimeModule())
            .readValue(json, typeRef)
}
