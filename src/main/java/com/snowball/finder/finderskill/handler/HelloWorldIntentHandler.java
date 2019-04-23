package com.snowball.finder.finderskill.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.finder.finderskill.client.BasicAuthClient;
import com.snowball.location.transport_api.response.PublicJourneyContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

public class HelloWorldIntentHandler implements RequestHandler {

    static final Logger logger = LoggerFactory.getLogger(HelloWorldIntentHandler.class);
    private BasicAuthClient client;
    Properties prop;

    public HelloWorldIntentHandler(BasicAuthClient client,Properties prop) {
        this.client = client;
        this.prop = prop;
    }

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName("HelloWorldIntent"));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        try {
            HttpResponse<String> response = client.getWithBasicAuth(
                    prop.getProperty("transportApi.endpoint") + "/hello",
                    null, null,
                    prop.getProperty("transportApi.username"),
                    prop.getProperty("transportApi.password"),
                    String.class);
            return returnResponse(input, response.getBody());
        } catch (UnirestException e) {
            String speechText = "Hello world did not work";
            return returnResponse(input, speechText);
        }
    }

    private Optional<Response> returnResponse(HandlerInput input, String responseText) {
        return input.getResponseBuilder()
                .withSpeech(responseText)
                .withSimpleCard("HelloWorld", responseText)
                .build();
    }
}
