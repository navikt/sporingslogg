package no.nav.sporingslogg.restapi;

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SuppressWarnings("serial")
public class CustomJsonObjectMapper extends ObjectMapper { // For å slippe å få så verbose timestamps i JSON

   public CustomJsonObjectMapper() { 
        super.registerModule(new JavaTimeModule());
        super.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }   
}
