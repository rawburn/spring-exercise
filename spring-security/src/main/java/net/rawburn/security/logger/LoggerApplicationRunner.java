package net.rawburn.security.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author rawburn·rc
 */
@Component
public class LoggerApplicationRunner implements ApplicationRunner {

    private static final Logger INFO_LOGGER = LoggerFactory.getLogger(LoggerApplicationRunner.class);
    private static final Logger KAFKA_LOGGER = LoggerFactory.getLogger("KAFKA_LOGGER");
    private static final Logger REQUEST_IP_LOGGER = LoggerFactory.getLogger("REQUEST_IP_LOGGER");

    @Override
    public void run(ApplicationArguments args) throws Exception {
        INFO_LOGGER.trace("TRACE!!");
        INFO_LOGGER.debug("DEBUG!!");
        INFO_LOGGER.info("INFO!!");
        INFO_LOGGER.warn("WARN!!");
        INFO_LOGGER.error("ERROR!!");

        KAFKA_LOGGER.trace("TRACE!!");
        KAFKA_LOGGER.debug("DEBUG!!");
        KAFKA_LOGGER.info("INFO!!");
        KAFKA_LOGGER.warn("WARN!!");
        KAFKA_LOGGER.error("ERROR!!");


        REQUEST_IP_LOGGER.trace("TRACE!!");
        REQUEST_IP_LOGGER.debug("DEBUG!!");
        REQUEST_IP_LOGGER.info("INFO!!");
        REQUEST_IP_LOGGER.warn("WARN!!");
        REQUEST_IP_LOGGER.error("ERROR!!");

        System.out.println(">> Down!!");
    }
}
