package org.telegram.telegrise.exceptions;

public class TelegRiseRuntimeException extends RuntimeException{
    public static RuntimeException unfold(Throwable e){
        if (e instanceof TelegRiseRuntimeException ex) return ex;
        if (e instanceof TranscriptionParsingException ex) return ex;
        else if (!(e instanceof TelegRiseInternalException)) return new TelegRiseRuntimeException(e.getMessage(), e, false);

        while (e.getCause() instanceof TelegRiseInternalException){
            e = e.getCause();
        }

        // After while statement, cause should be the last TelegRiseInternalException found, so we do one step forward
        e = e.getCause();

        return new TelegRiseRuntimeException(null, e, false);
    }


    public TelegRiseRuntimeException(String message) {
        super(message);
    }

    public TelegRiseRuntimeException(String message, Throwable throwable, boolean showStackTrace) {
        super(message, throwable, false, showStackTrace);
    }
}
