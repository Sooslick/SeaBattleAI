package ru.sooslick.seabattle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log {
    private static final Logger log = LoggerFactory.getLogger("MAIN");

    public static void info(String msg) {
        log.info(msg);
    }

    public static void warn(String msg) {
        log.warn(msg);
    }
}
