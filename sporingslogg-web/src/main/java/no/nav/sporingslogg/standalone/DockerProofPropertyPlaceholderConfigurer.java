package no.nav.sporingslogg.standalone;

import java.util.Properties;

public class DockerProofPropertyPlaceholderConfigurer extends org.springframework.beans.factory.config.PropertyPlaceholderConfigurer {

	// Håndterer propertynavn med CAPS LOCK
	
	@Override
	protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
		String normalWay = super.resolvePlaceholder(placeholder, props, systemPropertiesMode);
		if (normalWay == null) {
			return super.resolvePlaceholder(placeholder.toUpperCase(), props, systemPropertiesMode);
		}
		return normalWay;
	}

	@Override
	protected String resolvePlaceholder(String placeholder, Properties props) {
		String normalWay = super.resolvePlaceholder(placeholder, props);
		if (normalWay == null) {
			return super.resolvePlaceholder(placeholder.toUpperCase(), props);
		}
		return normalWay;
	}

}
