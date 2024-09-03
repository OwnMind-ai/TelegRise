package org.telegram.telegrise.core;

/**
 * Represents an element that has a name. Those types of elements will be stored in a {@link org.telegram.telegrise.core.parser.TranscriptionMemory}.
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

    String getName();
}
