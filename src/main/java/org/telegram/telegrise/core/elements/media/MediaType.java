package org.telegram.telegrise.core.elements.media;

import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrise.TelegRiseRuntimeException;
import org.telegram.telegrise.core.GeneratedValue;
import org.telegram.telegrise.core.ResourcePool;
import org.telegram.telegrise.core.elements.TranscriptionElement;
import org.telegram.telegrise.core.elements.actions.Send;

import java.util.List;

public interface MediaType extends TranscriptionElement {
    PartialBotApiMethod<?> createSender(Send parent, ResourcePool pool);
    List<InputMedia> createInputMedia(ResourcePool pool);
    GeneratedValue<Boolean> getWhen();

    default boolean isGroupable() {
        return true;
    }

    GeneratedValue<String> getFileId();
    GeneratedValue<String> getUrl();
    GeneratedValue<InputFile> getInputFile();

    default InputFile createInputFile(ResourcePool pool){
        String fileIdResult = generateNullableProperty(getFileId(), pool);
        if (fileIdResult != null)
            return new InputFile(fileIdResult);

        String urlResult = generateNullableProperty(getUrl(), pool);
        if (urlResult != null)
            return new InputFile(urlResult);

        InputFile fileResult = generateNullableProperty(getInputFile(), pool);
        if (fileResult != null)
            return fileResult;

        throw new TelegRiseRuntimeException("fileId, url or inputFile cannot be null");
    }

    default <T extends InputMedia> T createInputMedia(T instance, ResourcePool pool){
        InputFile inputFile = generateNullableProperty(getInputFile(), pool);
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
