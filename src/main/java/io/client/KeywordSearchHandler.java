package io.client;

import io.bytegate.annotation.Route;
import io.bytegate.model.HttpRequest;
import io.bytegate.model.HttpResponse;

public class KeywordSearchHandler {

    private final FileSearchService fileSearchService;

    public KeywordSearchHandler() {
        this.fileSearchService = new FileSearchService();
    }

    @Route(method = "GET", path = "/api/search")
    public HttpResponse search(HttpRequest request) {
        String keyword = request.getQueryParam("keyword");
        if (keyword == null || keyword.isEmpty()) {
            return HttpResponse.badRequest("Missing required query parameter: keyword");
        }

        String result = fileSearchService.search(keyword);
        return HttpResponse.ok(result);
    }
}
