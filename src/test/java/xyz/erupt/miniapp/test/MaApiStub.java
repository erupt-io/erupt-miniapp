package xyz.erupt.miniapp.test;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A stand-in for api.weixin.qq.com (Mini Program): canned JSON per path, and a record of what was called.
 */
public class MaApiStub {

    public record Call(String path, String body) {
    }

    private final HttpServer server;

    private final Map<String, String> responses = new ConcurrentHashMap<>();

    private final Map<String, String> contentTypes = new ConcurrentHashMap<>();

    private final List<Call> calls = new ArrayList<>();

    public MaApiStub() {
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        server.createContext("/", exchange -> {
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String path = exchange.getRequestURI().getPath();
            synchronized (calls) {
                calls.add(new Call(path, body));
            }
            byte[] response = responses.getOrDefault(path, "{\"errcode\":0,\"errmsg\":\"ok\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", contentTypes.getOrDefault(path, "application/json"));
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
        this.on("/cgi-bin/token", "{\"access_token\":\"stub-token\",\"expires_in\":7200}");
    }

    public String url() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    public MaApiStub on(String path, String json) {
        responses.put(path, json);
        return this;
    }

    // A binary answer, such as a code image
    public MaApiStub on(String path, String body, String contentType) {
        responses.put(path, body);
        contentTypes.put(path, contentType);
        return this;
    }

    public List<Call> calls(String path) {
        synchronized (calls) {
            return calls.stream().filter(it -> it.path().equals(path)).toList();
        }
    }

    public void reset() {
        synchronized (calls) {
            calls.clear();
        }
    }

    public void stop() {
        server.stop(0);
    }

}
