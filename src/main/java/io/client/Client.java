package io.client;

import io.bytegate.WebServer;
import io.bytegate.log.LogLevel;


public class Client {
    public static void main(String[] args) {
        NoteService noteService = new NoteService();

        WebServer server = new WebServer.Builder()
                .controller(new NoteController(noteService))
                .controller(new KeywordSearchHandler())
                .withDefaultParameters()
                .logLevel(LogLevel.DEBUG)
                .build();

        server.start();
    }
}
