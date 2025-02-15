package org.telegrise.telegrise.types;

/**
 * Size of a two-dimensional media object such as photo or video.
 * @param width wight of media
 * @param height height of media
 */
public record MediaSize(Integer width, Integer height) {}