package com.tritva.Evently.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "mpesa")
@Getter
@Setter
public class MpesaConfig {
    private String consumerKey;
    private String consumerSecret;
    private String passkey;
    private String shortCode;
    private String initiatorName;
    private String initiatorPassword;
    private String securityCredential;
    private String callbackUrl;
    private String apiUrl;
    private String oauthUrl;
}