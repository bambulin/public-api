package io.whalebone.publicapi.ejb;

import com.google.common.io.Resources;
import org.apache.commons.codec.Charsets;

import java.io.IOException;
import java.net.URL;

public class FileUtils {
    public static String resourceFileAsString(String fileName) throws IOException {
        URL url = Resources.getResource(fileName);
        return Resources.toString(url, Charsets.toCharset("UTF-8"));
    }
}
