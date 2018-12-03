package io.whalebone.publicapi.ejb;

import org.elasticsearch.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

public class FileUtils {
    public static String resourceFileAsString(String fileName) throws IOException {
        URL url = Resources.getResource(fileName);
        return Resources.toString(url, Charsets.UTF_8);
    }
}
