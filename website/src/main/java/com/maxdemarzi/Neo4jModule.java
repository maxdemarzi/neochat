package com.maxdemarzi;

import com.typesafe.config.Config;
import io.jooby.*;
import org.neo4j.driver.v1.*;

import javax.annotation.Nonnull;

public class Neo4jModule implements Extension {

    @Override
    public void install(@Nonnull Jooby application) throws Exception {
        Environment env = application.getEnvironment();
        Config conf = env.getConfig();

        Driver driver = GraphDatabase.driver(conf.getString("neo4j.uri"),
                AuthTokens.basic(conf.getString("neo4j.username"), conf.getString("neo4j.password")));

        ServiceRegistry registry = application.getServices();
        registry.put(Driver.class, driver);
        application.onStop(driver);
    }

}
