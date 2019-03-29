package io.whalebone.publicapi.ejb.elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ElasticClientProvider {
    private static final String ELASTIC_HOST_PREFIX = "ELASTIC_HOST";
    public static final HttpHost[] HOSTS = getHosts();

    @Inject
    private Logger logger;

    private RestHighLevelClient client;

    public RestHighLevelClient getClient() {
        if (client == null) {
            logger.log(Level.INFO, "Elastic client doesn't exists, creating new one.");

            // TODO timeouts to sys envs
            client = new RestHighLevelClient(
                    RestClient.builder(HOSTS)
                            .setRequestConfigCallback(requestConfigBuilder ->
                                    requestConfigBuilder.setConnectTimeout(10000)
                                            .setSocketTimeout(10000))
                            .setMaxRetryTimeoutMillis(10000)
            );
        }
        return client;
    }

    @PreDestroy
    public void shutDown() {
        if (client != null) {
            logger.log(Level.INFO, "Closing elastic client");
            try {
                client.close();
            } catch (IOException ioe) {
                logger.log(Level.SEVERE, "Cannot close elastic client.", ioe);
            }
        }
    }

    private static HttpHost[] getHosts() {
        List<HttpHost> hostList = new ArrayList<>();
        if (System.getenv().containsKey(ELASTIC_HOST_PREFIX)) {
            try {
                HttpHost host = HttpHost.create(System.getenv(ELASTIC_HOST_PREFIX));
                return new HttpHost[] {host};
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("System property " + ELASTIC_HOST_PREFIX + " doesn't contain valid url");
            }
        }
        int hostNumber = 1;
        while (System.getenv().containsKey(ELASTIC_HOST_PREFIX + "_" + hostNumber)) {
            try {
                hostList.add(HttpHost.create(System.getenv(ELASTIC_HOST_PREFIX + "_" + hostNumber)));
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("System property " + ELASTIC_HOST_PREFIX + "_" + hostNumber + " doesn't contain valid url");
            }
            hostNumber++;
        }
        if (hostList.isEmpty()) {
            throw new IllegalStateException("No elastic host is defined.");
        }
        return hostList.toArray(new HttpHost[0]);
    }
}
