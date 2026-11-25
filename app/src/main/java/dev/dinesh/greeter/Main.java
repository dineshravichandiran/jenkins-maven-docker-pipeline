package dev.dinesh.greeter;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/** Minimal HTTP service: /health for the load balancer, /greet?name= for a visible response. */
public final class Main {

    private Main() {
    }

    static HttpServer create(int port, String version) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/health", ex -> respond(ex, 200, "{\"status\":\"ok\",\"version\":\"" + version + "\"}"));
        server.createContext("/greet", ex -> respond(ex, 200, Greeter.greet(queryParam(ex.getRequestURI().getRawQuery(), "name"))));
        return server;
    }

    static String queryParam(String rawQuery, String key) {
        if (rawQuery == null) {
            return null;
        }
        for (String pair : rawQuery.split("&")) {
            int eq = pair.indexOf('=');
            String k = eq < 0 ? pair : pair.substring(0, eq);
            if (k.equals(key)) {
                return eq < 0 ? "" : URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private static void respond(HttpExchange ex, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        String version = System.getenv().getOrDefault("APP_VERSION", "dev");
        create(port, version).start();
        System.out.println("listening on " + port);
    }
}
