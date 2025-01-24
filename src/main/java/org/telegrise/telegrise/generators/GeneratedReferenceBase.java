package org.telegrise.telegrise.generators;

/**
 * This is a base interface for all variations of generated references.
 *
 * @see org.telegrise.telegrise.annotations.ReferenceGenerator
 * @since 0.8
 */
public sealed interface GeneratedReferenceBase
        permits GeneratedBiReference, GeneratedPolyReference, GeneratedReference, GeneratedVoidReference {
}
