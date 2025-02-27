package org.telegrise.telegrise.starter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "telegrise")
public class TelegRiseProperties {
    private String transcription;
}
