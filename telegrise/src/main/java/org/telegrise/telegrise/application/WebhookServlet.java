package org.telegrise.telegrise.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serial;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * This class represents a webhook bot application implementation, that uses provided executor service
 * ({@link Executors#newVirtualThreadPerTaskExecutor()} by default).
 * Instance of this class is meant to be used as a wrapper for {@link org.telegrise.telegrise.TelegramSessionsController TelegramSessionController}
 * that provides a concurrent execution (based on {@code executor}) and can be served by Jetty.
 *
 * @see ApplicationRunner
 * @since 0.10
 */
@Slf4j
class WebhookServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 459295714965072L;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Consumer<Update> updateConsumer;
    private final ExecutorService executor;
    private final boolean enableLogging;

    WebhookServlet(Consumer<Update> updateConsumer, @Nullable ExecutorService executor, boolean enableLogging) {
        this.updateConsumer = updateConsumer;
        this.executor = executor == null ? Executors.newVirtualThreadPerTaskExecutor() : executor;
        this.enableLogging = enableLogging;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final StringBuilder bodyString = new StringBuilder();
        String line;
        BufferedReader reader = req.getReader();
        while ((line = reader.readLine()) != null) {
            bodyString.append(line);
        }

        executor.submit(() -> {
            /*TODO HUGE (MAYBE) PERFORMANCE IMPROVEMENT POSSIBILITY
                We can leverage webhook's ability to return bot method as an answer to the request.
                Currently, webhooks bots behave as longpolling bots in this regard, making it less efficient.
                It is possible to capture the first BotApiMethod sent to BotSender to execute and pass it here,
                and execute other normally. However, this is really tricky and bug-prone to do.
                I am not sure if this even worth it. Maybe it will be beneficial to move session routing system here, idk.
            */
            try {
                updateConsumer.accept(mapper.readValue(bodyString.toString(), Update.class));
            } catch (IOException e) {
                log.error("Unable to convert request body to Update object", e);
            } finally {
                if (enableLogging)
                    log.info("'{}' request received from address '{}'", req.getMethod(), req.getRemoteAddr());
            }
        });

        resp.setStatus(200);
    }
}
