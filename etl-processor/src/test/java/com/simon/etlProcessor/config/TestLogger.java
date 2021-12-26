package com.simon.etlProcessor.config;


import org.junit.platform.commons.logging.LoggerFactory;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

public class TestLogger implements LambdaLogger {
    private static final org.junit.platform.commons.logging.Logger logger = LoggerFactory.getLogger(TestLogger.class);

    public void log(String message) {
        logger.info(() -> "Caught exception while closing extension context: ");

    }

    public void log(byte[] message) {
        logger.info(() -> new String(message));
    }
}