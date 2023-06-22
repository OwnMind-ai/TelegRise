package org.telegram.telegrise.core.expressions;

import lombok.Getter;

public class CharsStream {
    private final String text;

    @Getter
    private int position;

    public CharsStream(String input) {
        this.text = input;
    }

    public String next(int length) {
        String data = peek(length);
        this.position += length;

        return data;
    }

    public String peek(int length) {
        int maxIndex = Math.min(this.position + length, this.text.length());

        return this.text.substring(position, maxIndex);
    }

    public char next() {
        return this.text.charAt(this.position++);
    }

    public char peek() {
        if (eof()) return 0;

        return this.text.charAt(this.position);
    }

    public boolean eof() {
        return this.position >= this.text.length();
    }
}