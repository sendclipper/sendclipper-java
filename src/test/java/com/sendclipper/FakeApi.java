package com.sendclipper;

import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** A fake SendClipper API on the JDK's HttpServer: canned replies in order, every call recorded. */
final class FakeApi implements AutoCloseable {
  static final ObjectMapper MAPPER = new ObjectMapper();

  /** One request the fake received. */
  static final class Call {
    final String method;
    final String path; // raw (still percent-encoded)
    final Map<String, String> query;
    final Map<String, String> headers; // case-insensitive
    final byte[] body;

    Call(String method, String path, Map<String, String> query, Map<String, String> headers, byte[] body) {
      this.method = method;
      this.path = path;
      this.query = query;
      this.headers = headers;
      this.body = body;
    }

    String header(String name) {
      return headers.get(name);
    }

    String bodyText() {
      return new String(body, StandardCharsets.UTF_8);
    }

    JsonNode json() {
      try {
        return MAPPER.readTree(body);
      } catch (IOException e) {
        throw new AssertionError("body is not JSON: " + bodyText(), e);
      }
    }
  }

  /** One canned answer. */
  static final class Reply {
    int status = 200;
    Object body; // JSON-encoded unless a String or byte[]
    final Map<String, String> headers = new LinkedHashMap<>();
    boolean drop; // hang up without answering (a network error)
    Duration delay = Duration.ZERO;

    Reply header(String name, String value) {
      headers.put(name, value);
      return this;
    }

    Reply delay(Duration d) {
      delay = d;
      return this;
    }
  }

  static Reply ok(Object body) {
    Reply r = new Reply();
    r.body = body;
    return r;
  }

  static Reply status(int status, Object body) {
    Reply r = ok(body);
    r.status = status;
    return r;
  }

  static Reply apiError(int status, String code, String message) {
    return status(status, map("error", map("code", code, "message", message)));
  }

  static Reply drop() {
    Reply r = new Reply();
    r.drop = true;
    return r;
  }

  /** A LinkedHashMap from alternating keys and values. */
  static Map<String, Object> map(Object... kv) {
    Map<String, Object> m = new LinkedHashMap<>();
    for (int i = 0; i < kv.length; i += 2) {
      m.put((String) kv[i], kv[i + 1]);
    }
    return m;
  }

  static List<Object> list(Object... items) {
    List<Object> l = new ArrayList<>();
    Collections.addAll(l, items);
    return l;
  }

  private final HttpServer server;
  private final ExecutorService pool = Executors.newCachedThreadPool();
  private final List<Reply> replies = new ArrayList<>();
  private final List<Call> calls = new ArrayList<>();
  final List<Duration> slept = Collections.synchronizedList(new ArrayList<>());
  private final List<String> unexpected = Collections.synchronizedList(new ArrayList<>());

  FakeApi(Reply... replies) {
    Collections.addAll(this.replies, replies);
    try {
      server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
    server.createContext("/", this::serve);
    server.setExecutor(pool);
    server.start();
  }

  String url() {
    return "http://127.0.0.1:" + server.getAddress().getPort();
  }

  /** A client pointed at this fake, whose backoff waits are recorded instead of slept. */
  SendClipper client() {
    return builder().build();
  }

  SendClipper.Builder builder() {
    return SendClipper.builder()
        .apiKey("sc_live_test")
        .baseUrl(url())
        .env(name -> null)
        .sleeper(slept::add);
  }

  synchronized List<Call> calls() {
    return new ArrayList<>(calls);
  }

  Call call(int i) {
    List<Call> c = calls();
    if (i >= c.size()) {
      fail("only " + c.size() + " calls were made, wanted call " + i);
    }
    return c.get(i);
  }

  private void serve(HttpExchange ex) throws IOException {
    byte[] body = ex.getRequestBody().readAllBytes();
    Map<String, String> query = new LinkedHashMap<>();
    String raw = ex.getRequestURI().getRawQuery();
    if (raw != null) {
      for (String pair : raw.split("&")) {
        int eq = pair.indexOf('=');
        String k = URLDecoder.decode(eq < 0 ? pair : pair.substring(0, eq), StandardCharsets.UTF_8);
        String v = eq < 0 ? "" : URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8);
        query.put(k, v);
      }
    }
    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    ex.getRequestHeaders().forEach((k, v) -> headers.put(k, String.join(",", v)));
    Reply rep;
    synchronized (this) {
      calls.add(new Call(ex.getRequestMethod(), ex.getRequestURI().getRawPath(), query, headers, body));
      if (replies.isEmpty()) {
        unexpected.add(ex.getRequestMethod() + " " + ex.getRequestURI());
        rep = status(599, "no reply left");
      } else {
        rep = replies.remove(0);
      }
    }
    if (!rep.delay.isZero()) {
      try {
        Thread.sleep(rep.delay.toMillis());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
    if (rep.drop) {
      ex.close(); // before any header: the connection is closed with no answer
      return;
    }
    byte[] out;
    if (rep.body == null) {
      out = new byte[0];
    } else if (rep.body instanceof String) {
      out = ((String) rep.body).getBytes(StandardCharsets.UTF_8);
    } else if (rep.body instanceof byte[]) {
      out = (byte[]) rep.body;
    } else {
      out = MAPPER.writeValueAsBytes(rep.body);
      ex.getResponseHeaders().set("Content-Type", "application/json");
    }
    rep.headers.forEach((k, v) -> ex.getResponseHeaders().set(k, v));
    ex.sendResponseHeaders(rep.status, out.length == 0 ? -1 : out.length);
    if (out.length > 0) {
      try (OutputStream os = ex.getResponseBody()) {
        os.write(out);
      }
    }
    ex.close();
  }

  @Override
  public void close() {
    server.stop(0);
    pool.shutdownNow();
    if (!unexpected.isEmpty()) {
      fail("unexpected requests: " + unexpected);
    }
  }
}
