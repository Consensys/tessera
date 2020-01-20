package com.quorum.tessera.config.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class IntervalPropertyHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntervalPropertyHelper.class);

    private final Map<String, String> properties;

    public IntervalPropertyHelper(Map<String, String> properties) {
        this.properties = properties;
    }

    public long partyInfoInterval() {
        try {
            return Long.parseLong(properties.getOrDefault("partyInfoInterval", "5000"));
        }
        catch (NumberFormatException ex) {
            LOGGER.warn("Not able to parse configured property. Will use default value instead");
            return 5000L;
        }
    }

    public long enclaveKeySyncInterval() {
        try {
            return Long.parseLong(properties.getOrDefault("enclaveKeySyncInterval", "2000"));
        }
        catch (NumberFormatException ex) {
            LOGGER.warn("Not able to parse configured property. Will use default value instead");
            return 2000L;
        }
    }

    public long syncInterval() {
        try {
            return Long.parseLong(properties.getOrDefault("syncInterval", "60000"));
        }
        catch (NumberFormatException ex) {
            LOGGER.warn("Not able to parse configured property. Will use default value instead");
            return 60000L;
        }
    }
}
