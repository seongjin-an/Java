package com.hive.bee.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoggingController {
    Logger logger = LoggerFactory.getLogger(LoggingController.class);

    @RequestMapping("/")
    public String index(){
        logger.trace("A TRACE MESSAGE");
        logger.debug("A DEBUG MESSAGE");
        logger.info("AN INFO MESSAGE");
        logger.warn("A WARN MESSAGE");
        logger.error("AN ERROR MESSAGE");

        return "Howdy! Check out the Logs to see the output...";
    }
}
