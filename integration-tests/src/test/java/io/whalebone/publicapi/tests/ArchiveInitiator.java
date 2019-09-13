package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import io.whalebone.publicapi.ejb.PublicApiService;
import io.whalebone.publicapi.ejb.elastic.ElasticClientProvider;
import io.whalebone.publicapi.ejb.elastic.IoCElasticService;

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
    private static final String ELASTIC_ENDPOINT = ElasticClientProvider.ELASTIC_HOSTS[0].toURI();
    private static final String IOC_ELASTIC_ENDPOINT = System.getenv("IOC_ELASTIC_HOST_REST");
    private static final String IOCS_INDEX = IoCElasticService.IOCS_INDEX + "_v3";
    private static final String TIMESTAMP_PLACEHOLDER = "@TIMESTAMP@";
    private static final String THREAT_TYPE_PLACEHOLDER = "@THREAT_TYPE@";
    private WebClient webClient;

    public ArchiveInitiator() {
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
     * cleans all iocs
     */
    public void cleanIoCs() throws IOException {
        WebRequest requestSettings = new WebRequest(
                new URL(IOC_ELASTIC_ENDPOINT +
                        "/" + IoCElasticService.IOCS_INDEX + "*"), HttpMethod.DELETE);

        webClient.getPage(requestSettings);
    }

    public void createIoCsIndex() throws IOException {
        WebRequest requestSettings = new WebRequest(
                new URL(IOC_ELASTIC_ENDPOINT +
                        "/" + IOCS_INDEX), HttpMethod.PUT);

        webClient.getPage(requestSettings);
    }

    public void sendDnsLog(String filename, ZonedDateTime timestamp) throws IOException {
        sendJsonFile(filename, ELASTIC_ENDPOINT, createPassiveDnsIndex(timestamp), PublicApiService.PASSIVE_DNS_TYPE,
                TIMESTAMP_PLACEHOLDER, formatTimestamp(timestamp));
    }

    public void sendLogEvent(String filename, ZonedDateTime timestamp) throws IOException {
        sendJsonFile(filename, ELASTIC_ENDPOINT, createLogsIndex(timestamp), PublicApiService.LOGS_TYPE,
                TIMESTAMP_PLACEHOLDER, formatTimestamp(timestamp));
    }

    public void sendDnsSecLog(String filename, ZonedDateTime timestamp) throws IOException {
        sendJsonFile(filename, ELASTIC_ENDPOINT, createDnsSecIndex(timestamp), PublicApiService.DNSSEC_TYPE,
                TIMESTAMP_PLACEHOLDER, formatTimestamp(timestamp));
    }

    public void sendIoCOfThreatType(String filename, String threatType) throws IOException {
        sendJsonFile(filename, IOC_ELASTIC_ENDPOINT, IOCS_INDEX, IoCElasticService.IOCS_TYPE,
                THREAT_TYPE_PLACEHOLDER, threatType);
    }

    public void sendMultipleDnsLogs(String dirName, ZonedDateTime timestamp) throws IOException {
        sendMultipleFiles(dirName, ELASTIC_ENDPOINT, createPassiveDnsIndex(timestamp), PublicApiService.PASSIVE_DNS_TYPE,
                TIMESTAMP_PLACEHOLDER, formatTimestamp(timestamp));
    }

    public void sendMultipleLogEvents(String dirName, ZonedDateTime timestamp) throws IOException {
        sendMultipleFiles(dirName, ELASTIC_ENDPOINT, createLogsIndex(timestamp), PublicApiService.LOGS_TYPE,
                TIMESTAMP_PLACEHOLDER, formatTimestamp(timestamp));
    }

    public void sendMultipleDnsSecLogs(String dirName, ZonedDateTime timestamp) throws IOException {
        sendMultipleFiles(dirName, ELASTIC_ENDPOINT, createDnsSecIndex(timestamp), PublicApiService.DNSSEC_TYPE,
                TIMESTAMP_PLACEHOLDER, formatTimestamp(timestamp));
    }

    private void sendJsonFile(String fileName, String host, String index, String type, String placeholder, String replacement) throws IOException {
        Path file = Paths.get(ArchiveInitiator.class.getClassLoader().getResource(fileName).getPath());
        sendFile(file, host, index, type, placeholder, replacement);
    }

    private void sendMultipleFiles(String dirName, String host, String index, String type, String placeholder, String replacement)
            throws  IOException {
        Path path = Paths.get(ArchiveInitiator.class.getClassLoader().getResource(dirName).getPath());
        File dir = path.toFile();
        for (File file : dir.listFiles(File::isFile)) {
            sendFile(file.toPath(), host, index, type, placeholder, replacement);
        }
    }

    private void sendFile(Path file, String host, String index, String type, String placeholder, String replacement) throws IOException {
        byte[] encoded = Files.readAllBytes(file);
        String body = new String(encoded, StandardCharsets.UTF_8);
        body = body.replaceAll(placeholder, replacement);
        URL url = new URL(host + "/" + index + "/" + type + "?refresh=true");
        sendStringToUrl(body, url);
    }


    private void sendStringToUrl(String content, URL url) throws IOException {
        WebRequest requestSettings = new WebRequest(url, HttpMethod.POST);
        requestSettings.setAdditionalHeader("Content-Type" ,"application/json");
        requestSettings.setAdditionalHeader("Accept", "application/json;charset=UTF-8");
        requestSettings.setRequestBody(content);
        webClient.getPage(requestSettings);
    }

    private String formatTimestamp(ZonedDateTime timestamp) {
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
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
