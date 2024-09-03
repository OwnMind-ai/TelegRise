package org.telegram.telegrise.core.elements.text;

import org.telegram.telegrise.core.ResourcePool;

public interface TextConditionalElement{
    boolean isApplicable(ResourcePool pool);
    String getString(ResourcePool pool);
}
