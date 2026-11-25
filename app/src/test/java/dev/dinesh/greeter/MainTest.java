package dev.dinesh.greeter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void parsesQueryParameters() {
        assertEquals("Ann Lee", Main.queryParam("name=Ann%20Lee&x=1", "name"));
        assertNull(Main.queryParam("x=1", "name"));
        assertNull(Main.queryParam(null, "name"));
    }

    @Test
    void servesHealthAndGreetingOverHttp() throws Exception {
        HttpServer server = Main.create(0, "test-1");
        server.start();
        try {
            int port = server.getAddress().getPort();
            HttpClient client = HttpClient.newHttpClient();

            HttpResponse<String> health = client.send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/health")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, health.statusCode());
            assertEquals("{\"status\":\"ok\",\"version\":\"test-1\"}", health.body());

            HttpResponse<String> greet = client.send(
                    HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/greet?name=Dinesh")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals("Hello, Dinesh!", greet.body());
        } finally {
            server.stop(0);
        }
    }
}
