package no.nav.sporingslogg.kafka;

public class KafkaProperties {
	private String topic;
	private String groupId; 
	private String producerGroupId; 
	private String bootstrapServers;
	private String username;
	private String password;
	private String truststoreFile;
	private String truststorePassword;
	
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getProducerGroupId() {
		return producerGroupId;
	}
	public void setProducerGroupId(String producerGroupId) {
		this.producerGroupId = producerGroupId;
	}
	public String getBootstrapServers() {
		return bootstrapServers;
	}
	public void setBootstrapServers(String bootstrapServers) {
		this.bootstrapServers = bootstrapServers;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getTruststoreFile() {
		return truststoreFile;
	}
	public void setTruststoreFile(String truststoreFile) {
		this.truststoreFile = truststoreFile;
	}
	public String getTruststorePassword() {
		return truststorePassword;
	}
	public void setTruststorePassword(String truststorePassword) {
		this.truststorePassword = truststorePassword;
	}
}
