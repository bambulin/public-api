package io.whalebone.publicapi.ejb.elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.forarchive.client.Client;
import org.elasticsearch.forarchive.client.transport.TransportClient;
import org.elasticsearch.forarchive.common.settings.ImmutableSettings;
import org.elasticsearch.forarchive.common.transport.InetSocketTransportAddress;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ElasticClientProvider {
    private static final String ELASTIC_HOST_ENV = "ELASTIC_HOST";
    public static final HttpHost[] ELASTIC_HOSTS = getElasticHosts();
    private static final String IOC_ELASTIC_HOST_ENV = "IOC_ELASTIC_HOST";
    private static final InetSocketTransportAddress[] IOC_ELASTIC_HOSTS = getIoCElasticHosts();

    @Inject
    private Logger logger;

    // client for elastic where logs, passivedns and dnssec records are stored
    private RestHighLevelClient client;
    // client for elastic where iocs are archived (old version of elastic is used)
    private Client iocElasticClient;

    public RestHighLevelClient getClient() {
        if (client == null) {
            logger.log(Level.INFO, "Elastic client doesn't exists, creating new one.");

            // TODO timeouts to sys envs
            client = new RestHighLevelClient(
                    RestClient.builder(ELASTIC_HOSTS)
                            .setRequestConfigCallback(requestConfigBuilder ->
                                    requestConfigBuilder.setConnectTimeout(10000)
                                            .setSocketTimeout(10000))
                            .setMaxRetryTimeoutMillis(10000)
            );
        }
        return client;
    }

    public Client getIocElasticClient() {
        if (iocElasticClient == null) {
            logger.log(Level.INFO, "IoCElastic client doesn't exists, creating new one.");
            iocElasticClient = new TransportClient(ImmutableSettings.settingsBuilder()
                    //.put("cluster.name", "archive")
                    .put("client.transport.ignore_cluster_name", true)
                    .build()
            ).addTransportAddresses(IOC_ELASTIC_HOSTS);
        }
        return iocElasticClient;
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
        if (iocElasticClient != null) {
            logger.log(Level.INFO, "Closing IoCElastic client");
            try {
                iocElasticClient.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Cannot close IoCElastic client.", e);
            }
        }
    }

    private static HttpHost[] getElasticHosts() {
        List<HttpHost> hostList = new ArrayList<>();
        for (String host: getHosts(ELASTIC_HOST_ENV)) {
            try {
                hostList.add(HttpHost.create(host));
            } catch (IllegalArgumentException iae) {
                throw new IllegalArgumentException("Host '" + host + "' is not valid elastic url", iae);
            }
        }
        return hostList.toArray(new HttpHost[0]);
    }

    private static InetSocketTransportAddress[] getIoCElasticHosts() {
        List<InetSocketTransportAddress> hostAddressList = new ArrayList<>();
        for (String host: getHosts(IOC_ELASTIC_HOST_ENV)) {
            try {
                // tcp scheme is irrelevant since the client uses host and port only,
                // however URI needs scheme otherwise it throws exception
                URI hostUrl = new URI("tcp://" + host);
                hostAddressList.add(new InetSocketTransportAddress(hostUrl.getHost(), hostUrl.getPort()));
            } catch (URISyntaxException use) {
                throw new IllegalArgumentException("Host '" + host + "' is not valid elastic url", use);
            }
        }
        return hostAddressList.toArray(new InetSocketTransportAddress[0]);
    }

    private static String[] getHosts(String envProp) {
        if (System.getenv().containsKey(envProp)) {
            return new String[] {System.getenv(envProp)};
        }
        int hostNumber = 1;
        List<String> hostList = new ArrayList<>();
        while (System.getenv().containsKey(envProp + "_" + hostNumber)) {
            hostList.add(System.getenv(envProp + "_" + hostNumber));
            hostNumber++;
        }
        if (hostList.isEmpty()) {
            throw new IllegalStateException("Env prop '" + envProp + "' is not defined.");
        }
        return hostList.toArray(new String[0]);
    }
}
