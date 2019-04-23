package com.snowball.finder.finderskill.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;

import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class HelpIntentHandler implements RequestHandler  {
    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(intentName(IntentConstant.INTENT_HELP));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        String speechText = "Ask me to plan a journey for you. For example, you can say get me somewhere, or plan a journey, or get me to London";
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build();
    }
}
