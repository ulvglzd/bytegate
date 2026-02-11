package io.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NoteService {

    private final Map<Integer, String> notes = new ConcurrentHashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    public int save(String content) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        int id = idCounter.getAndIncrement();
        notes.put(id, content);
        return id;
    }

    public String findById(int id) {
        return notes.get(id);
    }
}
