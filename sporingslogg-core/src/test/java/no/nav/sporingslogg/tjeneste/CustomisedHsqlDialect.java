package no.nav.sporingslogg.tjeneste;

import java.sql.Types;

import org.hibernate.dialect.HSQLDialect;

public class CustomisedHsqlDialect extends HSQLDialect {

    public CustomisedHsqlDialect() {
        super();
        registerColumnType(Types.CLOB, "clob"); // Må til for å få skrevet mer enn X size i CLOB
    }
}
