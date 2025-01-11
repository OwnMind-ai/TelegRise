package org.telegrise.telegrise.core.elements.text;

import org.telegrise.telegrise.core.ResourcePool;

public interface TextConditionalElement{
    boolean isApplicable(ResourcePool pool);
    String getString(ResourcePool pool);
}
