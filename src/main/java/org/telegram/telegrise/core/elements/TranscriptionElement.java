package org.telegram.telegrise.core.elements;

import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.LocalNamespace;

import java.io.Serializable;

public interface TranscriptionElement extends Serializable {
    default LocalNamespace createNamespace(ApplicationNamespace global){
        return null;
    }
}
