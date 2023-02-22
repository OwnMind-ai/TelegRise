package org.telegram.telegrise.core.elements;

import org.telegram.telegrise.core.ApplicationNamespace;
import org.telegram.telegrise.core.LocalNamespace;

public interface TranscriptionElement {
    default LocalNamespace createNamespace(ApplicationNamespace global){
        return null;
    }
}
