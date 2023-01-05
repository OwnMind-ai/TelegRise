package org.telegram.telegrise.core.elements;

public enum KeyboardType {
    INLINE, REPLY;

    public static KeyboardType of(String value){
        switch (value.toLowerCase()){
            case "reply": return REPLY;
            case "inline": return INLINE;
            default: return null;
        }
    }
}
