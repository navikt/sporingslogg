package no.nav.pensjon.domain

import java.sql.Timestamp
import java.time.LocalDateTime
import javax.persistence.AttributeConverter

class TimestampConverter : AttributeConverter<LocalDateTime, Timestamp> {

    override fun convertToDatabaseColumn(localDateTime: LocalDateTime?): Timestamp? {
        return if (localDateTime == null) null else Timestamp.valueOf(localDateTime)
    }

    override fun convertToEntityAttribute(sqlTimestamp: Timestamp?): LocalDateTime? {
        return sqlTimestamp?.toLocalDateTime()
    }


}