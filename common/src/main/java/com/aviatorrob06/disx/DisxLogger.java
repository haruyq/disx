package com.aviatorrob06.disx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisxLogger {
    private static Logger logger = LoggerFactory.getLogger("Disx");
    public static void debug(Object input){
        if (DisxMain.debug) {
            logger.debug(String.valueOf(input));
        }
    }

    public static void error(Object input){
        logger.error(String.valueOf(input));
    }

    public static void info(Object input){
        logger.info(String.valueOf(input));
    }

    public static void warn(Object input){
        logger.warn(String.valueOf(input));
    }

}
