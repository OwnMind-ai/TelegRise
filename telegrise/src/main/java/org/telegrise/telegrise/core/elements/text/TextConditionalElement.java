package org.telegrise.telegrise.core.elements.text;

import org.telegrise.telegrise.core.ResourcePool;

/**
 * A base interface for elements that are part of condition branching in {@link Text} element
 */
public interface TextConditionalElement{
    boolean isApplicable(ResourcePool pool);
    String getString(ResourcePool pool);
}
