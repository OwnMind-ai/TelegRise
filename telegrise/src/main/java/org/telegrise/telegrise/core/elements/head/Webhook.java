package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@Element(name = "webhook")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Webhook extends NodeElement {
    // Allows developers to run test servers as a long-pool by using #env
    @Attribute(name = "enabled")
    private GeneratedValue<Boolean> enabled = GeneratedValue.GENERATED_TRUE;

    @Attribute(name = "url", nullable = false)
    private GeneratedValue<String> url;

    @Attribute(name = "certificate")
    private GeneratedValue<String> certificate;

    @Attribute(name = "ipAddress")
    private GeneratedValue<String> ipAddress;

    @Attribute(name = "maxConnections")
    private GeneratedValue<Integer> maxConnections;

    @Attribute(name = "allowedUpdates")
    private String[] allowedUpdates;

    @Attribute(name = "dropPendingUpdates")
    private GeneratedValue<Boolean> dropPendingUpdates;

    @Attribute(name = "secretToken")
    private GeneratedValue<String> secretToken;

    @Attribute(name = "port")
    private GeneratedValue<Integer> port;

    @Attribute(name = "enableRequestLogging")
    private GeneratedValue<Boolean> enableRequestLogging = GeneratedValue.GENERATED_FALSE;

    @Attribute(name = "useHttps")
    private GeneratedValue<Boolean> useHttps;

    @Attribute(name = "keyStorePath")
    private GeneratedValue<String> keyStorePath;

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
