package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import io.whalebone.publicapi.ejb.PublicApiService;
import io.whalebone.publicapi.ejb.elastic.ElasticClientProvider;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ArchiveInitiator {
    private static final String ELASTIC_ENDPOINT = ElasticClientProvider.HOSTS[0].toURI();
    private WebClient webClient;

    public ArchiveInitiator() {
        //elasticEndpoint = "http://" + System.getenv("ELASTIC_HOST") + ":" + System.getenv("ELASTIC_REST_PORT");
        webClient = new WebClient();
    }

    /**
     * cleans all dns logs
     */
    public void cleanDnsLogs() throws IOException {
        WebRequest requestSettings = new WebRequest(
                new URL(ELASTIC_ENDPOINT +
                        "/" + PublicApiService.PASSIVE_DNS_INDEX_PREFIX + "*"), HttpMethod.DELETE);
         webClient.getPage(requestSettings);
    }

    /**
     * cleans all dnssec logs
     */
    public void cleanDnsSecLogs() throws IOException {
        WebRequest requestSettings = new WebRequest(
                new URL(ELASTIC_ENDPOINT +
                        "/" + PublicApiService.DNSSEC_INDEX_PREFIX + "*"), HttpMethod.DELETE);

        webClient.getPage(requestSettings);
    }

    /**
     * cleans all log events
     */
    public void cleanEventLogs() throws IOException {
        WebRequest requestSettings = new WebRequest(
                new URL(ELASTIC_ENDPOINT +
                        "/" + PublicApiService.LOGS_INDEX_PREFIX + "*"), HttpMethod.DELETE);

        webClient.getPage(requestSettings);
    }

    /**
     * Loads json from file on classpath (in resources folder) and sends it to archive.
     * Timestamps are updated with timestamp using updateTimestamps method
     *
     * @param filename
     * @param timestamp
     * @throws IOException
     */
    public void sendDnsLog(String filename, ZonedDateTime timestamp) throws IOException {
        sendJsonFile(filename, createPassiveDnsIndex(timestamp), PublicApiService.PASSIVE_DNS_TYPE, timestamp);
    }

    /**
     * Similar to sendDnsJsonToArchive method but works with event logs indexes
     *
     * @param filename
     * @param timestamp
     */
    public void sendLogEvent(String filename, ZonedDateTime timestamp) throws IOException {
        sendJsonFile(filename, createLogsIndex(timestamp), PublicApiService.LOGS_TYPE, timestamp);
    }

    public void sendDnsSecLog(String filename, ZonedDateTime timestamp) throws IOException {
        sendJsonFile(filename, createDnsSecIndex(timestamp), PublicApiService.DNSSEC_TYPE, timestamp);
    }

    public void sendMultipleDnsLogs(String dirName, ZonedDateTime timestamp) throws IOException {
        sendMultipleFiles(dirName, createPassiveDnsIndex(timestamp), PublicApiService.PASSIVE_DNS_TYPE, timestamp);
    }

    public void sendMultipleLogEvents(String dirName, ZonedDateTime timestamp) throws IOException {
        sendMultipleFiles(dirName, createLogsIndex(timestamp), PublicApiService.LOGS_TYPE, timestamp);
    }

    public void sendMultipleDnsSecLogs(String dirName, ZonedDateTime timestamp) throws IOException {
        sendMultipleFiles(dirName, createDnsSecIndex(timestamp), PublicApiService.DNSSEC_TYPE, timestamp);
    }

    private void sendJsonFile(String fileName, String index, String type, ZonedDateTime timestamp) throws IOException {
        Path file = Paths.get(ArchiveInitiator.class.getClassLoader().getResource(fileName).getPath());
        sendFile(file, index, type, timestamp);
    }

    private void sendMultipleFiles(String dirName, String index, String type, ZonedDateTime timestamp)
            throws  IOException {
        Path path = Paths.get(ArchiveInitiator.class.getClassLoader().getResource(dirName).getPath());
        File dir = path.toFile();
        for (File file : dir.listFiles(File::isFile)) {
            sendFile(file.toPath(), index, type, timestamp);
        }
    }

    private void sendFile(Path file, String index, String type, ZonedDateTime timestamp) throws IOException {
        byte[] encoded = Files.readAllBytes(file);
        String body = new String(encoded, StandardCharsets.UTF_8);
        body = updateTimestamps(body, timestamp);
        URL url = new URL(ELASTIC_ENDPOINT + "/" + index + "/" + type + "?refresh=true");
        sendStringToUrl(body, url);
    }


    private void sendStringToUrl(String content, URL url) throws IOException {
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
        return text.replaceAll("@TIMESTAMP@", timestampFormatted);
    }

    private String createLogsIndex(ZonedDateTime timestamp) {
        String date = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return PublicApiService.LOGS_INDEX_PREFIX + date;
    }

    private String createPassiveDnsIndex(ZonedDateTime timestamp) {
        String date = timestamp.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        return PublicApiService.PASSIVE_DNS_INDEX_PREFIX + date;
    }

    private String createDnsSecIndex(ZonedDateTime timestamp) {
        String date = timestamp.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        return PublicApiService.DNSSEC_INDEX_PREFIX + date;
    }
}
