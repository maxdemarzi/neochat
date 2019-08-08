package com.maxdemarzi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fizzed.rocker.runtime.RockerRuntime;
import io.jooby.*;
import io.jooby.json.JacksonModule;
import io.jooby.rocker.RockerModule;
import org.mindrot.jbcrypt.BCrypt;
import org.neo4j.driver.v1.Driver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class App extends Jooby {

    {

        install(new Neo4jModule());
        install(new RockerModule());

        RockerRuntime.getInstance().setReloading(true);

        install(new JacksonModule());
        ObjectMapper mapper = require(ObjectMapper.class);
        mapper.setTimeZone(TimeZone.getTimeZone("UTC"));


        // Configure public static files
        Path assets = Paths.get("public/assets");
        assets("/assets/?*", assets);
        assets("/favicon.ico", "/public/assets/ico/favicon.ico");

        // Footer Pages
        get("/about", ctx -> views.footer.about.template());
        get("/cookies", ctx -> views.footer.cookies.template());
        get("/privacy", ctx -> views.footer.privacy.template());
        get("/terms",  ctx -> views.footer.terms.template());

        // Public Pages
        get("/", ctx -> views.index.template());
        get("/register", ctx -> views.register.template());
        post("/register", ctx -> {
            Formdata form = ctx.form();
            String id = form.get("id").toOptional().orElse("");
            String email = form.get("email").toOptional().orElse("");
            String password = form.get("password").toOptional().orElse("");
            password = BCrypt.hashpw(password, BCrypt.gensalt());
            Driver driver = require(Driver.class);
            Map<String, Object> user = CypherQueries.CreateUser(driver, id, email, password);
            if (user != null) {
                String token = CypherQueries.TokenizeUser(driver, id);
                if (token != null) {
                    // Set a token cookie for 2 hours
                    ctx.setResponseCookie(new Cookie("token", token).setMaxAge(7200));
                    ctx.session().put("id", (String)user.get("id"));
                    ctx.session().put("email", (String)user.get("email"));
                    return views.home.template((String)user.get("id"));
                }
            }
            return views.register.template();
        });

        get("/signin", ctx -> views.signin.template());
        post("/signin", ctx -> {
            Formdata form = ctx.form();
            String id = form.get("id").toOptional().orElse("");
            String password = form.get("password").toOptional().orElse("");
            Driver driver = require(Driver.class);
            Map<String, Object> user = CypherQueries.GetUser(driver, id);
            if (user != null && BCrypt.checkpw(password, (String) user.get("password"))) {
                String token = CypherQueries.TokenizeUser(driver, id);
                if (token != null) {
                    // Set a token cookie for 2 hours
                    ctx.setResponseCookie(new Cookie("token", token).setMaxAge(7200));
                    ctx.session().put("id", (String)user.get("id"));
                    ctx.session().put("email", (String)user.get("email"));
                    return views.home.template(id);
                }
            }
            return views.signin.template();
        });

        // Private pages - Sign in required for anything below this line
        decorator(next -> ctx -> {
            // If they have a id in this session, pass through
            if (ctx.session().get("id").valueOrNull() != null) {
                return next.apply(ctx);
            }
            // If they have a token, check it, set the session and pass through
            String token = ctx.cookie("token").valueOrNull();
            if(token != null) {
                Driver driver = require(Driver.class);
                Map<String, Object> user = CypherQueries.AuthorizeUser(driver, token);
                if (user != null) {
                    ctx.session().put("id", (String)user.get("id"));
                    ctx.session().put("email", (String)user.get("email"));
                    return next.apply(ctx);
                }
            }

            // Redirect for signin
            return ctx.sendRedirect("/signin");
        });

        get("/home", ctx -> {
            String id = ctx.session().get("id").value();
            return views.home.template(id);
        });

        get("/signout", ctx -> {
            ctx.session().destroy();
            ctx.setResponseCookie(new Cookie("token").setMaxAge(0));
            return ctx.sendRedirect("/");
        });

        post("/chat", ctx -> {
            String id = ctx.session().get("id").value();
            Formdata form = ctx.form();
            String chatText = form.get("chatText").toOptional().orElse("");
            Driver driver = require(Driver.class);
            List<Map<String, Object>> response = CypherQueries.Chat(driver, id, chatText);
            return response;
        });

    }

    public static void main(String[] args) {
        runApp(args, App::new);
    }
}