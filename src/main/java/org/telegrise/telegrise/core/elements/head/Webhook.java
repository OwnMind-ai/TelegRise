package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

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
    private GeneratedValue<Integer> port = GeneratedValue.ofValue(9091);

    @Attribute(name = "enableRequestLogging")
    private GeneratedValue<Boolean> enableRequestLogging = GeneratedValue.GENERATED_FALSE;

    @Attribute(name = "useHttps")
    private GeneratedValue<Boolean> useHttps = GeneratedValue.GENERATED_FALSE;

    @Attribute(name = "keyStorePath")
    private GeneratedValue<String> keyStorePath;

    @Attribute(name = "keyStorePassword")
    private GeneratedValue<String> keyStorePassword;
}
