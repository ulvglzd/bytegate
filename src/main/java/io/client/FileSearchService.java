package io.client;

import java.util.Random;

public class FileSearchService {

    private static final String SUCCESS_STRING = "Text file found: ";
    private static final String FAILURE_STRING = "Text file not found for keyword: ";

    public String search(String keyword) {
        Random random = new Random();
        boolean found = random.nextBoolean();
        if (found) {
            return SUCCESS_STRING + keyword + ".txt";
        } else {
            return FAILURE_STRING + keyword;
        }
    }
}
