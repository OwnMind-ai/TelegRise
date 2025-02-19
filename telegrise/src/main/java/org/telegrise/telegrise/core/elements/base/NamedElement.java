package org.telegrise.telegrise.core.elements.base;

import org.telegrise.telegrise.core.parser.TranscriptionMemory;

/**
 * Represents an element that has a name, will be stored in a {@link TranscriptionMemory} by that name
 * and can be retrieved by {@link org.telegrise.telegrise.TranscriptionManager TranscriptionManager}.
 *
 * @since 0.7
 */
public interface NamedElement {
    /**
     * Indicates if the element is global. Global elements are stored in the global storage of the
     * <a href="org.telegram.telegrise.core.parser.TranscriptionMemory">TranscriptionMemory</a>
     * and can be accessed from any other element (like text.byName and transition.target) <b>even if they exist in different trees</b>.
     *
     * @return true if the element is global, false otherwise.
     */
    default boolean isGlobal(){ return false; }

    /**
     * The name of the element
     */
    String getName();
}
