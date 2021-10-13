package dev.damith.simpletlv;

import java.io.InputStream;
import java.net.URL;

public class CustomFileReader {



    public static InputStream getFileFromResources(String fileName) {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        InputStream stream = classloader.getResourceAsStream(fileName);
        if (stream == null) {
            throw new IllegalArgumentException("file is not found!");
        } else {
            return stream;

        }

    }
}