package com.campus.client.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientLogger {
    private static final Logger LOGGER = LoggerFactory.getLogger("campus-client");

    public static void info(String msg) { LOGGER.info(msg); }
    public static void warning(String msg) { LOGGER.warn(msg); }
    public static void severe(String msg) { LOGGER.error(msg); }
}
