package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.webhook.WebhookOptions;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

@Element(name = "webhook")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Webhook extends NodeElement {
    // Allows developers to run test servers as a long-pool by using #env
    @Attribute(name = "enabled")
    private GeneratedValue<Boolean> enabled = GeneratedValue.ofValue(true);

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
    private GeneratedValue<Boolean> enableRequestLogging;

    @Attribute(name = "useHttps")
    private GeneratedValue<Boolean> useHttps;

    @Attribute(name = "keyStorePath")
    private GeneratedValue<String> keyStorePath;

    @Attribute(name = "keyStorePassword")
    private GeneratedValue<String> keyStorePassword;

    public WebhookOptions produceOptions(){
        var resources = new ResourcePool();
        var result = new WebhookOptions();

        if (port != null) result.setPort(port.generate(resources));
        if (enableRequestLogging != null) result.setEnableRequestLogging(enableRequestLogging.generate(resources));
        if (useHttps != null) result.setUseHttps(useHttps.generate(resources));
        if (keyStorePath != null) result.setKeyStorePath(keyStorePath.generate(resources));
        if (keyStorePassword != null) result.setKeyStorePassword(keyStorePassword.generate(resources));

        return result;
    }
}
