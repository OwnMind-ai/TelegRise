package core.elements;

public enum KeyboadType {
    INLINE, REPLY;

    public static KeyboadType of(String value){
        switch (value.toLowerCase()){
            case "reply": return REPLY;
            case "inline": return INLINE;
            default: return null;
        }
    }
}
