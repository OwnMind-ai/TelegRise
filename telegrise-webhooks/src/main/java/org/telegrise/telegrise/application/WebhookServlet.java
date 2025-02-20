package org.telegrise.telegrise.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegrise.telegrise.core.TelegramSessionsController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serial;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * This class represents a webhook bot application implementation, that uses provided executor service
 * ({@link Executors#newVirtualThreadPerTaskExecutor()} by default).
 * Instance of this class is meant to be used as a wrapper for {@link TelegramSessionsController TelegramSessionController}
 * that provides a concurrent execution (based on {@code executor}) and can be served by Jetty.
 *
 * @see ApplicationRunner
 * @since 0.10
 */
class WebhookServlet extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 459295714965072L;
    private static final String SECRET_TOKEN_HEADER = "X-Telegram-Bot-Api-Secret-Token";
    private static final Logger log = LoggerFactory.getLogger(WebhookServlet.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final Consumer<Update> updateConsumer;
    private final ExecutorService executor;
    private final String secretToken;

    WebhookServlet(Consumer<Update> updateConsumer, @Nullable ExecutorService executor, String secretToken) {
        this.updateConsumer = updateConsumer;
        this.executor = executor == null ? Executors.newVirtualThreadPerTaskExecutor() : executor;
        this.secretToken = secretToken;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (secretToken != null && !secretToken.equals(req.getHeader(SECRET_TOKEN_HEADER))) {
            log.warn("Webhook server encountered request with invalid secret token from {}. Bot will ignore this request." +
                    " Expected secret token '{}' but received '{}'", req.getRemoteAddr(), secretToken, req.getHeader(SECRET_TOKEN_HEADER));

            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

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
                and execute others normally. However, this is really tricky and bug-prone to do.
                I am not sure if this even worth it.
            */
            try {
                updateConsumer.accept(mapper.readValue(bodyString.toString(), Update.class));
            } catch (IOException e) {
                log.error("Unable to convert request body to Update object", e);
            } finally {
                log.trace("'{}' request received from address '{}'", req.getMethod(), req.getRemoteAddr());
            }
        });

        resp.setStatus(HttpServletResponse.SC_OK);
    }
}
