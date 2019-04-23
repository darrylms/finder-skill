package com.snowball.finder.finderskill.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.Response;
import com.amazon.ask.request.Predicates;
import com.snowball.finder.finderskill.service.journey.JourneyService;

import java.util.Map;
import java.util.Optional;

import static com.amazon.ask.request.Predicates.intentName;

public class RepeatJourneyIntentRequestHandler implements RequestHandler {

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName(IntentConstant.INTENT_JOURNEY_REPEAT).or(intentName(IntentConstant.INTENT_REPEAT)));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        try {
            Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();
            String responseText = (String) attributes.get(JourneyService.ATTR_LAST_JOURNEY);
            attributes.put(JourneyService.ATTR_LAST_JOURNEY, responseText);
            input.getAttributesManager().setSessionAttributes(attributes);
            return returnResponse(input, responseText);
        } catch (Exception e) {
            return returnResponse(input, JourneyService.NO_PREVIOUS_JOURNEY_MSG);
        }
    }

    private Optional<Response> returnResponse(HandlerInput input, String responseText) {
        return input.getResponseBuilder()
                .withSpeech(responseText)
                .withReprompt(responseText)
                .withSimpleCard("JourneySession", responseText)
                .withShouldEndSession(false)
                .build();
    }
}
