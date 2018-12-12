package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import io.whalebone.publicapi.ejb.elastic.ElasticService;


import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ArchiveInitiator {
    String elasticEndpoint;
    WebClient webClient;

    public ArchiveInitiator() {

        elasticEndpoint = "http://" + System.getenv("ELASTIC_HOST") + ":" + System.getenv("ELASTIC_REST_PORT");
        webClient = new WebClient();
    }

    /**
     * cleans all dns logs
     */
    public void cleanDnsLogs() throws IOException {
        WebRequest requestSettings = new WebRequest(
                new URL(elasticEndpoint +
                        "/" + ElasticService.PASSIVE_DNS_INDEX), HttpMethod.DELETE);
         webClient.getPage(requestSettings);
    }

    /**
     * cleans all dns logs
     */
    public void cleanEventLogs() throws IOException {
        WebRequest requestSettings = new WebRequest(
                new URL(elasticEndpoint +
                        "/" + ElasticService.LOGS_INDEX), HttpMethod.DELETE);

        webClient.getPage(requestSettings);
    }
    /**
     * Loads json from file on classpath (in resources folder) and sends it to archive.
     * Timestamps are updated with timestamp using updateTimestamps method
     * @param filename name of file on classpath
     * @param timestamp
     */
    public void sendDnsJsonToArchive(String filename, ZonedDateTime timestamp) throws IOException {
        String date = timestamp.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        URL url = new URL(elasticEndpoint +
                "/" + ElasticService.PASSIVE_DNS_INDEX.replace("*", "-") + date + "/"+ ElasticService.PASSIVE_DNS_TYPE + "?refresh=true");
        //TODO:this is not nice, maybe PASSIVE_DNS_INDEX shouldn't contain the *?

        Path path = Paths.get(ArchiveInitiator.class.getClassLoader().getResource(filename).getPath());

        byte[] encoded = Files.readAllBytes(path);
        String body = new String(encoded, StandardCharsets.UTF_8);
        body = updateTimestamps(body, timestamp);

        sendStringToUrl(body, url);
    }

    /**
     * Similar to sendDnsJsonToArchive method but works with event logs indexes
     * @param filename
     * @param timestamp
     */
    public void sendLogEventJsonToArchive(String filename, ZonedDateTime timestamp) throws IOException {
        String date = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        URL url = new URL(elasticEndpoint +
                        "/" + ElasticService.LOGS_INDEX.replace("*","-") + date + "/"+ ElasticService.LOGS_TYPE + "?refresh=true");

        Path path = Paths.get(ArchiveInitiator.class.getClassLoader().getResource(filename).getPath());

        byte[] encoded = Files.readAllBytes(path);
        String body = new String(encoded,StandardCharsets.UTF_8);
        body = updateTimestamps(body, timestamp);

        sendStringToUrl(body, url);
        }

    /**
     * Loads json from resource file and sends it to archive using bulk api.
     * The json is supposed to contain bulk api json.
     * updates timestamps
     * see https://www.elastic.co/guide/en/elasticsearch/reference/1.7/docs-bulk.html
     * @param filename
     * @param timestamp
     */
    public void sendBulkJsonToArchive(String filename,ZonedDateTime timestamp) throws IOException {
        URL url = new URL(elasticEndpoint + "/_bulk");

        Path path = Paths.get(ArchiveInitiator.class.getClassLoader().getResource(filename).getPath());

        byte[] encoded = Files.readAllBytes(path);
        String body = new String(encoded, StandardCharsets.UTF_8);
        body = updateTimestamps(body, timestamp);

        sendStringToUrl(body, url);
        }

    void sendStringToUrl(String content, URL url) throws IOException {
        WebRequest requestSettings = new WebRequest(url, HttpMethod.POST);
        requestSettings.setAdditionalHeader("Content-Type" ,"application/json");
        requestSettings.setAdditionalHeader("Accept", "application/json;charset=UTF-8");

        requestSettings.setRequestBody(content);

        webClient.getPage(requestSettings);
    }

    /**
     * Updates timestamps in text with timestamps corresponding to Calendar timestamp
     * @param text
     * @param timestamp
     * @return text with updated timestamps
     */
    public String updateTimestamps(String text, ZonedDateTime timestamp) {
        String timestampFormatted = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
        return text.replaceAll(
                        "\"timestamp\" *: *\".*?\"",
                        "\"timestamp\": \"" + timestampFormatted + "\"")
                    .replaceAll(
                        "\"logged\" *: *\".*?\"",
                        "\"logged\": \"" + timestampFormatted + "\"");
    }
}
