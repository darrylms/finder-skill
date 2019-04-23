package com.snowball.finder.finderskill;

import com.amazon.ask.Skill;
import com.amazon.ask.SkillStreamHandler;
import com.amazon.ask.Skills;
import com.snowball.finder.finderskill.client.BasicAuthClient;
import com.snowball.finder.finderskill.handler.*;
import com.snowball.finder.finderskill.interceptor.InitPersistentAttributesInterceptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class FinderSkillStreamHandler extends SkillStreamHandler {

    private static Skill getSkill() {
        Properties prop = readProperties();
        BasicAuthClient client = new BasicAuthClient();
        return Skills.standard()
                .addRequestHandlers(
                        new CancelandStopIntentHandler(),
                        new PublicJourneyIntentHandler(client, prop),
                        new HelloWorldIntentHandler(client, prop),
                        new AlternatePublicJourneyIntentHandler(client, prop),
                        new RepeatJourneyIntentRequestHandler(),
                        new HelpIntentHandler(),
                        new LaunchRequestHandler(),
                        new SessionEndedRequestHandler())
                //.withSkillId("")
                //.addResponseInterceptor(new PersistenceSavingResponseInterceptor())
                .addResponseInterceptor(new InitPersistentAttributesInterceptor())
                /* Enable persistent session storage */
                .withTableName(prop.getProperty("persistent.dynamoDB.table"))
                .withAutoCreateTable(true)
                .build();
    }

    public FinderSkillStreamHandler() {
        super(getSkill());
    }

    public static Properties readProperties() {
        Properties prop = new Properties();
        InputStream input;
        try {
            input = new FileInputStream("application.properties");
            prop.load(input);
        } catch (IOException e) {
            /* TODO: Something useful */
        }
        return prop;
    }
}
