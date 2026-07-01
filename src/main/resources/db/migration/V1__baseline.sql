-- Baseline: eksisterende skjema i Oracle, kjøres IKKE av Flyway (baselineOnMigrate=true).
-- Dokumenterer tabellstrukturen slik den ser ut i produksjon.

CREATE SEQUENCE SPORINGS_LOGG_ID_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;

CREATE TABLE SPORINGS_LOGG (
    ID              NUMBER          NOT NULL,
    PERSON          VARCHAR2(11)    NOT NULL,
    MOTTAKER        VARCHAR2(9)     NOT NULL,
    TEMA            VARCHAR2(3)     NOT NULL,
    HJEMMEL         VARCHAR2(100)   NOT NULL,
    TIDSPUNKT       TIMESTAMP       NOT NULL,
    LEVERTE_DATA    CLOB            NOT NULL,
    SAMTYKKE_TOKEN  VARCHAR2(3995),
    FORESPORSEL     CLOB,
    LEVERANDOR      VARCHAR2(9),
    CONSTRAINT PK_SPORINGS_LOGG PRIMARY KEY (ID)
);
