package com.hive.bee;

import com.hive.bee.socket.SocketServer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
@Component
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BeeApplication.class);
    }

    @PostConstruct
    public void initialize(){
        SocketServer.getInstance(22222).init();
    }
}
