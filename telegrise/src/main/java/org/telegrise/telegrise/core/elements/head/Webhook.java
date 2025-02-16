package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * This element enables webhook bot server and configures its parameters.
 * <p>
 * If this element is defined in {@code <head>} block and {@code enable} is true (by default),
 * then the bot will be run as webhook bot on specified {@code url}.
 * Application will automatically run <a href="https://core.telegram.org/bots/api#setwebhook">setWebhook</a>
 * method with provided arguments as attributes of this element.
 * <p>
 * Usually defining the {@code url} attribute is enough to successfully run the application:
 * port and http/s will be configured based on provided url automatically.
 * Provide {@code certificate} (a path to file) and {@code keyStorePassword} (if any) to use
 * <a href="https://core.telegram.org/bots/self-signed">self-sighed certificate</a>.
 * <pre>
 * {@code
 * <webhook url="http://127.0.0.1:8443"/>
 * <webhook url="https://my.bot.org:8080"
 *          certificate="path/to/certificate"
 *          keyStorePassword="*****"/>
 * }
 *
 * @since 0.10
 */
@Slf4j
@Element(name = "webhook")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Webhook extends NodeElement {
    /**
     * If true, bot will use configuration of this element to run webhook server.
     */
    // Allows developers to run test servers as a long-pool by using #env
    @Attribute(name = "enabled")
    private GeneratedValue<Boolean> enabled = GeneratedValue.GENERATED_TRUE;

    /**
     * URL to send updates to.
     */
    @Attribute(name = "url", nullable = false)
    private GeneratedValue<String> url;

    /**
     * Path to your certificate that will be sent to Telegram Server for verification purposes.
     * This attribute would autofill attribute {@code keyStorePath} that is used to configure SSL context.
     * Define {@code keyStorePath} separately if you want a different path to be used for that purpose.
     */
    @Attribute(name = "certificate")
    private GeneratedValue<String> certificate;

    /**
     * The fixed IP address which will be used to send webhook requests instead of the IP address resolved through DNS.
     */
    @Attribute(name = "ipAddress")
    private GeneratedValue<String> ipAddress;

    /**
     * The maximum allowed number of simultaneous HTTPS connections to the webhook for update delivery, 1-100.
     * Defaults to 40.
     */
    @Attribute(name = "maxConnections")
    private GeneratedValue<Integer> maxConnections;

    /**
     * List of the update types you want your bot to receive.
     * For example,
     * specify {@code "message; edited_channel_post; callback_query"} to only receive updates of these types.
     * See <a href="https://core.telegram.org/bots/api#update">Update</a> for a complete list of available update types.
     */
    @Attribute(name = "allowedUpdates")
    private String[] allowedUpdates;

    /**
     * Set to {@code true} to drop all pending updates.
     */
    @Attribute(name = "dropPendingUpdates")
    private GeneratedValue<Boolean> dropPendingUpdates;

    /**
     * A secret token to be sent in a header in every webhook request, 1-256 characters.
     * Only characters {@code A-Z}, {@code a-z}, {@code 0-9}, {@code _} and {@code -} are allowed.
     */
    @Attribute(name = "secretToken")
    private GeneratedValue<String> secretToken;

    /**
     * Port to use for the webhook server. Currently supported: <b>443, 80, 88, 8443</b>.
     */
    @Attribute(name = "port")
    private GeneratedValue<Integer> port;

    /**
     * If true, webhook server will use the provided keystore path and password to create SSL context.
     */
    @Attribute(name = "useHttps")
    private GeneratedValue<Boolean> useHttps;

    /**
     * Path to the key store.
     */
    @Attribute(name = "keyStorePath")
    private GeneratedValue<String> keyStorePath;

    /**
     * Password to the key store.
     */
    @Attribute(name = "keyStorePassword")
    private GeneratedValue<String> keyStorePassword;

    @Override
    protected void validate(TranscriptionMemory memory) {
        var pool = new ResourcePool();

        try {
            URI uri = new URI(getUrl().generate(pool));

            if(useHttps == null)
                useHttps = GeneratedValue.ofValue("https".equals(uri.getScheme()));

            int port = uri.getPort();
            if (port < 0)
                port = switch (uri.getScheme()){
                    case "http" -> 80;
                    case "https" -> 443;
                    default -> port;
                };

            if (this.port != null && port != this.port.generate(pool)) {
                log.warn("Mismatch between specified 'port' field ({}) and URL's port ({})", port, this.port.generate(pool));
            } else if (this.port == null && port > 0) {
                log.info("Assuming webhook server port to be '{}'", port);
                this.port = GeneratedValue.ofValue(port);
            } else
                throw new TranscriptionParsingException("Unable to determine webhook server port. Please, specify it in 'port' attribute or in 'url' itself.", node);
        } catch (URISyntaxException e){
            throw new TranscriptionParsingException("Invalid 'url': " + e.getLocalizedMessage(), node);
        }
    }

    @Override
    public void load(TranscriptionMemory memory) {
        if (certificate != null && keyStorePath == null)
            keyStorePath = certificate;
    }

    public SetWebhook produceSetWebhook(ResourcePool pool) {
        return SetWebhook.builder()
                .url(getUrl().generate(pool))
                .certificate(getCertificate() == null ? null : new InputFile().setMedia(new File(getCertificate().generate(pool))))
                .dropPendingUpdates(GeneratedValue.generate(getDropPendingUpdates(), pool))
                .ipAddress(GeneratedValue.generate(getIpAddress(), pool))
                .secretToken(GeneratedValue.generate(getSecretToken(), pool))
                .maxConnections(GeneratedValue.generate(getMaxConnections(), pool))
                .allowedUpdates(getAllowedUpdates() == null ? List.of() : List.of(getAllowedUpdates()))
                .build();
    }
}
