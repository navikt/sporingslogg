package no.nav.sporingslogg.standalone.testconfig;

public class UrlUsernamePassword {

	private final String url;
	private final String username;
	private final String password;
	
	public UrlUsernamePassword(String url, String username, String password) {
		this.url = url;
		this.username = username;
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
