package com.thesett.util.config.shiro;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ShiroConfiguration {
    static final String DEFAULT_SECURED_URL_PATTERN = "/*";

    @JsonProperty
    private boolean enabled = false;

    @JsonProperty("url_pattern")
    private String securedUrlPattern = DEFAULT_SECURED_URL_PATTERN;

    @JsonProperty("session_handler")
    private boolean dropwizardSessionHandler = false;

    public boolean isEnabled() {
        return enabled;
    }

    public String getSecuredUrlPattern() {
        return securedUrlPattern;
    }

    public boolean isDropwizardSessionHandler() {
        return dropwizardSessionHandler;
    }
}
