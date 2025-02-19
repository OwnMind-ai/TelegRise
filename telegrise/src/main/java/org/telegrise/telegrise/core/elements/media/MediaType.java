package org.telegrise.telegrise.core.elements.media;

import org.telegram.telegrambots.meta.api.methods.botapimethods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegrise.telegrise.core.ResourcePool;
import org.telegrise.telegrise.core.elements.actions.Send;
import org.telegrise.telegrise.core.elements.base.NodeElement;
import org.telegrise.telegrise.core.expressions.GeneratedValue;
import org.telegrise.telegrise.core.parser.TranscriptionMemory;
import org.telegrise.telegrise.exceptions.TelegRiseRuntimeException;
import org.telegrise.telegrise.exceptions.TranscriptionParsingException;

import java.util.List;

/**
 * A base class for elements that represent Telegram media and other interactive message types.
 * <p>
 * If {@link #isGroupable()} is true
 * then implementation can be used in media groups and can be applied more than once in API methods.
 * If implementation is the only media provided, then API method (typically {@link Send}) will ask the implementation
 * for the appropriate API method for that type of media.
 *
 * @since 0.1
 */
public abstract class MediaType extends NodeElement {
    public abstract PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool);
    public abstract List<InputMedia> createInputMedia(ResourcePool pool);
    public abstract GeneratedValue<Boolean> getWhen();

    public boolean isGroupable() { return true; }
    public boolean isMediaRequired(){ return true; }

    public abstract GeneratedValue<String> getFileId();
    public abstract GeneratedValue<String> getUrl();
    public abstract GeneratedValue<InputFile> getInputFile();

    @Override
    public void validate(TranscriptionMemory memory) {
        if (isMediaRequired() && getFileId() == null && getUrl() == null  && getInputFile() == null)
            throw new TranscriptionParsingException("No media source found: url, fileId, inputFile", node);
    }

    public InputFile createInputFile(ResourcePool pool){
        String fileIdResult = generateNullableProperty(getFileId(), pool);
        if (fileIdResult != null)
            return new InputFile(fileIdResult);

        String urlResult = generateNullableProperty(getUrl(), pool);
        if (urlResult != null)
            return new InputFile(urlResult);

        InputFile fileResult = generateNullableProperty(getInputFile(), pool);
        if (fileResult != null)
            return fileResult;

        throw new TelegRiseRuntimeException("fileId, url and inputFile cannot be null", node);
    }

    public <T extends InputMedia> T createInputMedia(T instance, ResourcePool pool){
        InputFile inputFile = generateNullableProperty(getInputFile(), pool);

        // A workaround for required-args-constructor since 7.*
        instance.setMedia(null);

        if (inputFile != null) {
            instance.setMediaName(inputFile.getMediaName());
            instance.setMedia(inputFile.getNewMediaFile(), inputFile.getMediaName());
            instance.setNewMedia(true);
        } else {
            String fileId = generateNullableProperty(getFileId(), pool);
            instance.setMedia(fileId != null ? fileId : generateNullableProperty(getUrl(), pool));
        }

        return instance;
    }
}
