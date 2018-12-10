package io.whalebone.publicapi.tests;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;


import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ArchiveInitiator {

    /**
     * cleans all indices matching index
     @param index: string to be matched against in elastic
     */
    static public void cleanLogs(String index) throws IOException {

        WebClient webClient = new WebClient();
        WebRequest requestSettings = new WebRequest(
                new URL("http://" + System.getenv("ELASTIC_HOST") + ":" + System.getenv("ELASTIC_REST_PORT") +
                        "/" + index), HttpMethod.DELETE);

         webClient.getPage(requestSettings);
    }

    /**
     * Loads json from file on classpath (in resources folder) and sends it to archive.
     * Timestamps are updated with timestamp using updateTimestamps method
     * @param filename name of file on classpath
     * @param timestamp
     */
    static public void sendDnsJsonToArchive(String filename, Calendar timestamp) throws IOException {
        String date = new SimpleDateFormat("yyyy.MM.dd").format(timestamp.getTime());
        URL url = new URL("http://" + System.getenv("ELASTIC_HOST") + ":" + System.getenv("ELASTIC_REST_PORT") +
                "/" + "passivedns-" + date + "/logs");

        File file = new File(ArchiveInitiator.class.getClassLoader().getResource(filename).getFile());

        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        String body = new String(encoded);
        body = updateTimestamps(body, timestamp);

        sendStringToUrl(body , url);
    }

    /**
     * Similar to sendDnsJsonToArchive method but works with event logs indexes
     * @param filename
     * @param timestamp
     */
    static public void sendLogEventJsonToArchive(String filename, Calendar timestamp) throws  IOException {
        String date = new SimpleDateFormat("yyyy-MM-dd").format(timestamp.getTime());
        URL url = new URL("http://" + System.getenv("ELASTIC_HOST") + ":" + System.getenv("ELASTIC_REST_PORT") +
                        "/" + "logs-"+ date + "/match");

        File file = new File(ArchiveInitiator.class.getClassLoader().getResource(filename).getFile());

        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        String body = new String(encoded);
        body = updateTimestamps(body, timestamp);

        sendStringToUrl(body , url);
        }

    /**
     * Loads json from resource file and sends it to archive using bulk api.
     * The json is supposed to contain bulk api json.
     * updates timestamps
     *
     * see https://www.elastic.co/guide/en/elasticsearch/reference/1.7/docs-bulk.html
     *
     * @param filename
     * @param timestamp
     */
    static public void sendBulkJsonToArchive(String filename, Calendar timestamp) throws IOException {

        URL url = new URL("http://" + System.getenv("ELASTIC_HOST") + ":" + System.getenv("ELASTIC_REST_PORT") +
                        "/_bulk");

        File file = new File(ArchiveInitiator.class.getClassLoader().getResource(filename).getFile());

        byte[] encoded = Files.readAllBytes(Paths.get(file.getPath()));
        String body = new String(encoded);
        body = updateTimestamps(body, timestamp);

        sendStringToUrl(body , url);
        }

    static void sendStringToUrl(String content, URL url) throws IOException {
        WebClient webClient = new WebClient();
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
    static public String updateTimestamps(String text, Calendar timestamp) {
        String timestampnow = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(timestamp.getTime());
        return text.replaceAll(
                        "\"timestamp\":\".*?\"",
                        "\"timestamp\": \"" + timestampnow + "\"")
                    .replaceAll(
                        "\"@timestamp\":\".*?\"",
                        "\"@timestamp\": \"" + timestampnow + "\"")
                    .replaceAll(
                        "\"logged\":\".*?\"",
                        "\"logged\": \"" + timestampnow + "\"");
    }

}
