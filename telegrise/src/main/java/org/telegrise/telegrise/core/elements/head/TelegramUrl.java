package org.telegrise.telegrise.core.elements.head;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegrise.telegrise.core.GeneratedValue;
import org.telegrise.telegrise.core.elements.NodeElement;
import org.telegrise.telegrise.core.parser.Attribute;
import org.telegrise.telegrise.core.parser.Element;

@Element(name = "telegramUrl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TelegramUrl extends NodeElement {
    @Attribute(name = "enabled")
    private GeneratedValue<Boolean> enabled = GeneratedValue.GENERATED_TRUE;

    @Attribute(name = "schema", nullable = false)
    private GeneratedValue<String> schema;

    @Attribute(name = "host", nullable = false)
    private GeneratedValue<String> host;

    @Attribute(name = "port", nullable = false)
    private GeneratedValue<Integer> port;

    @Attribute(name = "testServer", nullable = false)
    private GeneratedValue<Boolean> testServer;
}
