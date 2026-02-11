package io.client;

import io.bytegate.annotation.Route;
import io.bytegate.model.HttpRequest;
import io.bytegate.model.HttpResponse;

public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @Route(method = "GET", path = "/api/notes/{id}")
    public HttpResponse getNote(HttpRequest request) {
        String idParam = request.getPathParam("id");

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            return HttpResponse.badRequest("Invalid note id: " + idParam);
        }

        String note = noteService.findById(id);
        if (note == null) {
            return HttpResponse.notFound("Note not found with id: " + id);
        }

        return HttpResponse.ok(note);
    }

    @Route(method = "POST", path = "/api/notes")
    public HttpResponse createNote(HttpRequest request) {
        String body = request
                .bodyPublisher()
                .orElseThrow(() -> new IllegalArgumentException("Request body is required"))
                .toString();

        if (body.isBlank()) {
            return HttpResponse.badRequest("Note content cannot be empty");
        }

        int id = noteService.save(body);
        return HttpResponse.ok("Note created with id: " + id);
    }
}
