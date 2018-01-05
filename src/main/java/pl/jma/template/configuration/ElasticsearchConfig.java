package pl.jma.template.configuration;

import static org.slf4j.LoggerFactory.getLogger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {
	
	private static final Logger LOG = getLogger(ElasticsearchConfig.class);	
	
	private Client client;
	
	@SuppressWarnings("resource")
	@Bean
	public Client getClient() throws UnknownHostException {
		
		Settings settings = Settings.builder()
		        .put("cluster.name", "elasticsearch")
		        .build();
		
		client = new PreBuiltTransportClient(settings)
		        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		
		LOG.info("--ElasticSearch--");
        Map<String, String> asMap = client.settings().getAsMap();
        asMap.forEach((k, v) -> LOG.info(k + " = " + v));
        LOG.info("--ElasticSearch--");

		return client;		
	}
	
	@PreDestroy
	public void closeClient() {
		client.close();
	}

}
