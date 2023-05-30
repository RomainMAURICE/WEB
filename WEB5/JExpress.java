import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.System.out;
import static java.util.Collections.shuffle;
import static java.util.Map.entry;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.IntStream.range;

/**
 * An express.js-like application framework, requires Java 17
 *
 * Compile the application with : javac JExpress.java
 * Run the application with     : java JExpress
 * Get the documentation with   : javadoc -d ../doc JExpress.java
 */
@SuppressWarnings("restriction")
public class JExpress {
  /**
   * A HTTP request
   */
  public interface Request {
    /**
     * The HTTP method of the request.
     * @return the HTTP method.
     */
    String method();

    /**
     * The HTTP path of the request.
     * @return the HTTP path.
     */
    String path();

    /**
     * Get named route parameter
     * @param name name of the parameter
     * @return the value of the parameter or ""
     */
    String param(String name);

    /**
     * Returns the body of the request
     * @return the body of the request
     */
    InputStream body();

    /**
     * Returns the body of the request as a String.
     * @return the body of the request as a String.
     * @throws IOException if an I/O error occurs.
     */
    public String bodyText() throws IOException;
  }

  private static Request request(HttpExchange exchange) {
    return new Request() {
      @Override
      public String method() {
        return exchange.getRequestMethod().toUpperCase(Locale.ROOT);
      }

      @Override
      public String path() {
        return exchange.getRequestURI().getPath();
      }

      @Override
      @SuppressWarnings("unchecked")
      public String param(String name) {
        return ((Map<String, String>) exchange.getAttribute("params")).getOrDefault(name, "");
      }

      @Override
      public InputStream body() {
        return exchange.getRequestBody();
      }

      @Override
      public String bodyText() throws IOException {
        try(var in = exchange.getRequestBody();
            var reader = new InputStreamReader(in, UTF_8);
            var buffered = new BufferedReader(reader)) {
          return buffered.lines().collect(joining("\n"));
        }
      }
    };
  }

  /**
   * A HTTP response
   */
  public interface Response {
    /**
     * Set the HTTP status of the response
     * @param status the HTTP status (200, 404, etc)
     * @return the current response.
     */
    Response status(int status);

    /**
     * Appends the specified value to the HTTP response header field.
     * @param field a header field name
     * @param value the value of the header field
     * @return the current response.
     */
    Response append(String field, String value);

    /**
     * Sets the response’s HTTP header field to value
     * @param field a header field name
     * @param value the value of the header field
     * @return the current response.
     */
    Response set(String field, String value);

    /**
     * Sets the Content-Type HTTP header to the MIME type.
     * @param type the MIME type.
     * @return the current response.
     */
    Response type(String type);

    /**
     * Sets the Content-Type HTTP header and the charset encoding.
     * @param type the MIME type.
     * @param charset the charset encoding
     * @return the current response.
     */
    default Response type(String type, String charset) {
      return type(type + "; charset=" + charset);
    }

    /**
     * Sets the Content-Type HTTP header and the charset encoding.
     * @param type the MIME type.
     * @param charset the charset encoding
     * @return the current response.
     */
    default Response type(String type, Charset charset) {
      return type(type, charset.name());
    }

    /**
     * Sends a JSON response with the correct 'Content-Type'.
     * @param object an object, can be an iterable, a stream, a record or a map
     * @throws IOException if an I/O error occurs.
     */
    void json(Object object) throws IOException;

    /**
     * Sends a JSON response with the correct 'Content-Type'.
     * @param json a JSON string
     * @throws IOException if an I/O error occurs.
     */
    void json(String json) throws IOException;

    /**
     * Send a Text response.
     * If the status is not defined 200 will be used.
     * If the Content-Type is not defined, "text/html" will be used.
     * @param body the text of the response
     * @throws IOException if an I/O error occurs.
     */
    void send(String body) throws IOException;

    /**
     * Send a file as response.
     * The status is set to 200 if the file is found or 404 if not found.
     * @param path the path of the file;
     * @throws IOException if an I/O error occurs.
     */
    void sendFile(Path path) throws IOException;
  }

  private static String toJSON(Object o) {
    if (o instanceof Collection<?> collection) {
      return toJSONArray(collection.stream());
    }
    if (o instanceof Iterable<?> iterable) {
      return toJSONArray(StreamSupport.stream(spliteratorUnknownSize(iterable.iterator(), 0), false));
    }
    if (o instanceof Map<?,?> map) {
      return toJSONObject(map);
    }
    if (o instanceof Record record) {
      return toJSONObject(record);
    }
    throw new IllegalStateException("unknown json object " + o);
  }
  private static String toJSONItem(Object item) {
    if (item == null) {
      return "null";
    }
    if (item instanceof String) {
      return "\"" + item + '"';
    }
    if (item instanceof Boolean || item instanceof Integer || item instanceof Double) {
      return item.toString();
    }
    return toJSON(item);
  }
  private static String toJSONArray(Stream<?> stream) {
    return stream.map(JExpress::toJSONItem).collect(joining(", ", "[", "]"));
  }
  private static String toJSONObject(Map<?,?> map) {
    return map.entrySet().stream()
        .map(e -> "\"" + e.getKey() + "\": " + toJSONItem(e.getValue()))
        .collect(joining(", ", "{", "}"));
  }
  private static Object accessor(Method accessor, Record record) {
    try {
      return accessor.invoke(record);
    } catch (IllegalAccessException e) {
      throw (IllegalAccessError) new IllegalAccessError().initCause(e);
    } catch (InvocationTargetException e) {
      var cause = e.getCause();
      if (cause instanceof RuntimeException runtimeException) {
        throw runtimeException;
      }
      if (cause instanceof Error error) {
        throw error;
      }
      throw new UndeclaredThrowableException(cause);
    }
  }
  private static String toJSONObject(Record record) {
    return Arrays.stream(record.getClass().getRecordComponents())
        .map(c -> "\"" + c.getName() + "\": " + toJSONItem(accessor(c.getAccessor(), record)))
        .collect(joining(", ", "{", "}"));
  }

  private static Response response(HttpExchange exchange) {
    return new Response() {
      @Override
      public Response status(int status) {
        exchange.setAttribute("status", status);
        return this;
      }

      @Override
      public Response append(String field, String value) {
        exchange.getResponseHeaders().add(field, value);
        return this;
      }

      @Override
      public Response set(String field, String value) {
        exchange.getResponseHeaders().set(field, value);
        return this;
      }

      @Override
      public Response type(String type) {
        return set("Content-Type", type);
      }

      @Override
      public void json(Object object) throws IOException {
        json(toJSON(object));
      }

      @Override
      public void json(String json) throws IOException {
        type("application/json", "utf-8");
        send(json);
      }

      @Override
      public void send(String body) throws IOException {
        var content = body.getBytes(UTF_8);
        var status = (int) exchange.getAttribute("status");
        var contentLength = content.length;
        exchange.sendResponseHeaders(status, contentLength);
        System.err.println("  send " + status + " content-length " + contentLength);

        var headers = exchange.getResponseHeaders();
        if (!headers.containsKey("Content-Type")) {
          type("text/html", "utf-8");
        }
        try(var output = exchange.getResponseBody()) {
          output.write(content);
          output.flush();
        }
      }

      @Override
      public void sendFile(Path path) throws IOException {
        try {
          try(var input = Files.newInputStream(path)) {
            var contentLength = Files.size(path);
            exchange.sendResponseHeaders(200, contentLength);
            System.err.println("  send file " + 200 + " content-length " + contentLength);

            var headers = exchange.getResponseHeaders();
            if (!headers.containsKey("Content-Type")) {
              var contentType = Files.probeContentType(path);
              if (contentType == null) {
                contentType = "application/octet-stream";
              }
              //System.err.println("inferred content type " + contentType);
              if (contentType.startsWith("text/")) {
                type(contentType, "utf-8");
              } else {
                type(contentType);
              }
            }

            try(var output = exchange.getResponseBody()) {
              var buffer = new byte[8192];
              int read;
              while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
              }
              output.flush();
            }
          }
        } catch(FileNotFoundException e) {
          var message = "Not Found " + e.getMessage();
          //System.err.println(message);
          status(404).send("<html><h2>" + message + "</h2></html>");
        }
      }
    };
  }

  /**
   * A callback called to process a HTTP request in order to
   * create a HTTP response.
   */
  @FunctionalInterface
  public interface Callback {
    /**
     * Called to process a HTTP request in order to create a HTTP response.
     * @param request a HTTP request
     * @param response a HTTP response
     * @throws IOException if an I/O occurs
     */
    public void accept(Request request, Response response) throws IOException;
  }

  private static Function<String, Optional<Map<String, String>>> matcher(String uri) {
    var parts = uri.split("/");
    var length = parts.length;
    var predicate =  (Predicate<String[]>) components -> components.length >= length;
    var consumer = (BiConsumer<String[], Map<String,String>>) (_1, _2) -> { /* empty */ };
    for(var i = 0; i < length; i++) {
      var index = i;
      var part = parts[i];
      if (part.startsWith(":")) {
        var key = part.substring(1);
        var c = consumer;
        consumer = (components, map) -> { c.accept(components, map); map.put(key, components[index]); };
      } else {
        predicate = predicate.and(cs -> part.equals(cs[index]));
      }
    }

    var p =  predicate;
    var c = consumer;
    return path -> {
      var components = path.split("/");
      if (!p.test(components)) {
        return Optional.empty();
      }
      var map = new HashMap<String,String>();
      c.accept(components, map);
      return Optional.of(map);
    };
  }

  private JExpress() {
    // empty
  }

  /**
   * Creates an Express like application.
   * @return a new Express like application.
   */
  public static JExpress express() {
    return new JExpress();
  }

  interface Pipeline {
    void accept(HttpExchange exchange) throws IOException;
  }

  private static Pipeline asPipeline(Callback callback) {
    return exchange -> callback.accept(request(exchange), response(exchange));
  }

  private Pipeline pipeline = asPipeline((request, response) -> {
    var message = "no match " + request.method() + " " + request.path();
    response.status(404).send("<html><h2>" + message + "</h2></html>");
  });

  /**
   * Routes an HTTP request if the HTTP method is GET.
   * @param path a string representation of a path with interpolation.
   * @param callback a callback called when a client emits a
   *        HTTP request to create an HTTP response.
   */
  public void get(String path, Callback callback) {
    method("GET", path, callback);
  }

  /**
   * Routes an HTTP request if the HTTP method is POST.
   * @param path a string representation of a path with interpolation.
   * @param callback a callback called when a client emits a
   *        HTTP request to create an HTTP response.
   */
  public void post(String path, Callback callback) {
    method("POST", path, callback);
  }

  /**
   * Routes an HTTP request if the HTTP method is PUT.
   * @param path a string representation of a path with interpolation.
   * @param callback a callback called when a client emits a
   *        HTTP request to create an HTTP response.
   */
  public void put(String path, Callback callback) {
    method("PUT", path, callback);
  }

  /**
   * Routes an HTTP request if the HTTP method is DELETE.
   * @param path a string representation of a path with interpolation.
   * @param callback a callback called when a client emits a
   *        HTTP request to create an HTTP response.
   */
  public void delete(String path, Callback callback) {
    method("DELETE", path, callback);
  }

  private void method(String method, String path, Callback callback) {
    var oldPipeline = this.pipeline;
    var matcher = matcher(path);
    var stub = asPipeline(callback);
    this.pipeline = exchange -> {
      Optional<Map<String,String>> paramsOpt;
      if (exchange.getRequestMethod().equalsIgnoreCase(method) &&
          (paramsOpt = matcher.apply(exchange.getRequestURI().getPath())).isPresent()) {
        exchange.setAttribute("params", paramsOpt.get());
        stub.accept(exchange);
      } else {
        oldPipeline.accept(exchange);
      }
    };
  }

  /**
   * Starts a server on the given port and listen for connections.
   * @param port a TCP port
   * @throws IOException if an I/O error occurs.
   */
  public void listen(int port) throws IOException {
    var server = HttpServer.create(new InetSocketAddress(port), 0);
    server.createContext("/", exchange -> {
      System.err.println("request " + exchange.getRequestMethod() + " " + exchange.getRequestURI());
      try {
        exchange.setAttribute("status", 200);
        pipeline.accept(exchange);
        //exchange.close();
      } catch(Exception e) {
        e.printStackTrace();
        throw e;
      }
    });
    server.setExecutor(null);
    server.start();
  }



  // ---------------------------------------------------------- //
  //  DO NOT EDIT ABOVE THIS LINE                               //
  // ---------------------------------------------------------- //



  public static void main(String[] args) throws IOException {
  	var random = new Random(0);
  	var tickers = List.of(
  			"AAPL", "GOOG", "INTC", "MSFT", "HPQ", "KO", "WMT", "HOG");

  	var stocks = new HashMap<String,Integer>();
  	tickers.forEach(ticker -> {
  		stocks.put(ticker, 100 + random.nextInt(200));
  	});

    var app = express();
    app.get("/:text", (req, res) -> {
      var text = req.param("text");
      if (text.endsWith(".html")) {
      	out.println("serve html " + text);
        res.type("text/html", "utf-8").sendFile(Paths.get(text));
      } else if (text.endsWith(".js")) {
      	out.println("serve js " + text);
        res.type("text/javascript", "utf-8").sendFile(Paths.get(text));
      } else if (text.endsWith(".json")) {
      	out.println("serve json " + text);
        res.type("application/json", "utf-8").sendFile(Paths.get(text));
      } else {
      	res.status(404).type("text/html", "utf-8").send("<html>unknown resource " + text + "</html>");
      }
    });

    app.get("/api/ticker", (req, res) -> {
    	out.println("get all tickers " + tickers);
    	res.json(tickers);
    });
    app.post("/api/stock", (req, res) -> {
    	tickers.forEach(ticker -> { // 21 because the market always goes up :)
    		stocks.merge(ticker, random.nextInt(21) - 10, Integer::sum);
    	});
    	var tickerOrders = new ArrayList<>(tickers);
    	shuffle(tickerOrders, random);
    	var map = tickerOrders.stream().collect(toMap(ticker -> ticker, stocks::get, Integer::sum, LinkedHashMap::new));
    	out.println("provides stock values " + map);
    	res.json(map);
    });

    app.listen(8080);

    out.println("application started on port 8080");
  }
}
