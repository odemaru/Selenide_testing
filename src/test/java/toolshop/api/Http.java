package toolshop.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static toolshop.config.TestConfig.CONFIG;

/**
 * Транспортный слой для обращений к API магазина.
 *
 * API здесь не объект тестирования, а инструмент: через него тесты заводят
 * пользователя, вместо того чтобы каждый раз набивать форму регистрации из
 * двенадцати полей. Поэтому DSL для проверок ответа не нужен, и вместо
 * RestAssured взят HttpClient из JDK — на одну зависимость меньше.
 *
 * Ответ возвращается как есть, вместе с кодом статуса. Решение о том, считать
 * ли ответ успешным, принимает вызывающий: транспорт про смысл запроса
 * не знает.
 */
public final class Http {

    private static final Logger log = LoggerFactory.getLogger(Http.class);
    private static final int LOG_BODY_LIMIT = 300;

    static final ObjectMapper MAPPER = new ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private final String baseUrl = CONFIG.apiUrl();

    public record Response(int status, JsonNode body) {

        public String text(String field) {
            JsonNode value = body.path(field);
            return value.isMissingNode() ? null : value.asText();
        }
    }

    public Response post(String path, Object payload) {
        return send("POST", path, payload, null);
    }

    public Response post(String path, Object payload, String token) {
        return send("POST", path, payload, token);
    }

    public Response get(String path, String token) {
        return send("GET", path, null, token);
    }

    private Response send(String method, String path, Object payload, String token) {
        HttpRequest.BodyPublisher content = payload == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(toJson(payload));

        HttpRequest.Builder request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method(method, content);

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        log.info("--> {} {}{}", method, path, payload == null ? "" : " " + shorten(toJson(payload)));
        HttpResponse<String> response = execute(request.build());
        log.info("<-- {} {}", response.statusCode(), shorten(response.body()));

        return new Response(response.statusCode(), parse(response.body()));
    }

    private HttpResponse<String> execute(HttpRequest request) {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new IllegalStateException("Запрос к API не удался: " + request.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Запрос к API прерван: " + request.uri(), e);
        }
    }

    private JsonNode parse(String body) {
        try {
            return MAPPER.readTree(body == null || body.isBlank() ? "{}" : body);
        } catch (IOException e) {
            throw new IllegalStateException("Ответ API не разбирается как JSON: " + shorten(body), e);
        }
    }

    private String toJson(Object payload) {
        try {
            return MAPPER.writeValueAsString(payload);
        } catch (IOException e) {
            throw new IllegalStateException("Не удалось собрать тело запроса", e);
        }
    }

    private String shorten(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= LOG_BODY_LIMIT
                ? value
                : value.substring(0, LOG_BODY_LIMIT) + "... [" + value.length() + " символов]";
    }
}
