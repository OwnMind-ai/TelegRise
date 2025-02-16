package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.ApplicationNamespace;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Defines a telegram url to be used for API calls.
 * <pre>
 * {@code
 * <telegramUrl url="http://127.0.0.1:8081"/>
 * <telegramUrl schema="http" host="127.0.0.1" port="8081" testServer="false"/>
 * }
 *
 * @since 0.10
 */
@Element(name = "telegramUrl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TelegramUrl extends NodeElement {
    /**
     * If true, bot will use configuration of this element to make API calls.
     */
    @Attribute(name = "enabled")
    private GeneratedValue<Boolean> enabled = GeneratedValue.GENERATED_TRUE;

    /**
     * Url to the Telegram server.
     * This attribute is used as an alternative to defining
     * {@code schema}, {@code host} and {@code port} attributes and must contain those values in specified url,
     * for example: {@code "http://127.0.0.1:8081"}
     */
    @Attribute(name = "url")
    private GeneratedValue<String> url;

    /**
     * Schema of the url, like {@code http} or {@code https}.
     */
    @Attribute(name = "schema")
    private GeneratedValue<String> schema;

    /**
     * Host to which API calls should be made.
     */
    @Attribute(name = "host")
    private GeneratedValue<String> host;

    /**
     * Port to which API calls should be made.
     */
    @Attribute(name = "port")
    private GeneratedValue<Integer> port;

    /**
     * If true, '/test' will be added to the URL.
     */
    @Attribute(name = "testServer")
    private GeneratedValue<Boolean> testServer = GeneratedValue.GENERATED_FALSE;

    @Override
    public void validate(TranscriptionMemory memory, ApplicationNamespace namespace) {
        if (url != null && (schema != null || host != null || port != null))
            throw new TranscriptionParsingException("Element must either 'url' attribute or partitioned configuration " +
                    "('schema', 'host' and 'port'), but not both", node);
        else if (url == null && (schema == null || host == null || port == null))
            throw new TranscriptionParsingException("Incomplete Telegram URL definition: 'url' attribute or partitioned" +
                    " configuration ('schema', 'host' and 'port') must be present", node);
    }

    public org.telegram.telegrambots.meta.TelegramUrl produceTelegramUrl(ResourcePool pool){
        if (url == null){
            return new org.telegram.telegrambots.meta.TelegramUrl(
                    schema.generate(pool), host.generate(pool), port.generate(pool), testServer.generate(pool));
        } else {
            try {
                URI uri = new URI(getUrl().generate(pool));

                int port = uri.getPort();
                if (port < 0)
                    port = switch (uri.getScheme()){
                        case "http" -> 80;
                        case "https" -> 443;
                        default -> port;
                    };

                return new org.telegram.telegrambots.meta.TelegramUrl(
                        uri.getScheme(), uri.getHost(), port, testServer.generate(pool));
            } catch (URISyntaxException e){
                throw new TranscriptionParsingException("Invalid 'url': " + e.getLocalizedMessage(), node);
            }
        }
    }
}
