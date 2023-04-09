package org.telegram.telegrise.core.elements.text;

import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;

public interface TextConditionalElement extends TranscriptionElement{
    boolean isApplicable(ResourcePool pool);
    String getString(ResourcePool pool);
}
