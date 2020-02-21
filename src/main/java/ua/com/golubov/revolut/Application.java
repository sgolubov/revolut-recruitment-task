package ua.com.golubov.revolut;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

import static spark.Spark.after;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.port;

@Slf4j
public class Application {

    private final HelloMessageService helloMessageService;
    private final ObjectMapper objectMapper;

    @Inject
    Application(final HelloMessageService helloMessageService, final ObjectMapper objectMapper) {
        this.helloMessageService = helloMessageService;
        this.objectMapper = objectMapper;
    }

    public static void main(final String... args) {
        Guice.createInjector(new GuiceModule())
                .getInstance(Application.class)
                .run(8080);
    }

    void run(final int port) {
        port(port);

        before("/*", (req, res) -> log.info(String.format("%s: %s", req.requestMethod(), req.uri())));

        get("/", (req, res) -> {
            final HelloMessage message = helloMessageService.sayHello();
            return objectMapper.writeValueAsString(message);
        });

        after("/*", (req, res) -> log.info(res.body()));
    }
}
