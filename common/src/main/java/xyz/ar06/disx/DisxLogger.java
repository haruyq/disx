package xyz.ar06.disx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisxLogger {
    private static final Logger logger = LoggerFactory.getLogger("disx");
    public static void debug(Object input){
        if (DisxMain.debug) {
            logger.info(String.valueOf(input));
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
