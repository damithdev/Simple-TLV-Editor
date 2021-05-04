package dev.damith.simpletlv;

import java.net.URL;

public class CustomFileReader {



    public static String getFileFromResources(String fileName) {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        URL resource = classloader.getResource(fileName);
        if (resource == null) {

            throw new IllegalArgumentException("file is not found!");
        } else {
            return resource.getFile();//new java.io.File(resource.getFile());
        }

    }
}