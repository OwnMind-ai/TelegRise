package org.telegram.telegrise.core.elements;

import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.LocalNamespace;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.parser.ParserMemory;
import org.w3c.dom.Node;

import java.io.Serializable;

public interface TranscriptionElement extends Serializable {
    default LocalNamespace createNamespace(ApplicationNamespace global){
        return null;
    }

    default void validate(Node node){}
    default void load(ParserMemory memory){}

    default <T> T generateNullableProperty(GeneratedValue<T> property, ResourcePool pool){
        return property == null ? null : property.generate(pool);
    }
}
