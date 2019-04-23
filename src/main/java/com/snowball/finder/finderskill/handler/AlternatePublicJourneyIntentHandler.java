package com.snowball.finder.finderskill.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import com.amazon.ask.model.services.ServiceException;
import com.amazon.ask.model.services.directive.DirectiveService;
import com.amazon.ask.model.services.directive.Header;
import com.amazon.ask.model.services.directive.SendDirectiveRequest;
import com.amazon.ask.model.services.directive.SpeakDirective;
import com.amazon.ask.request.Predicates;
import com.snowball.finder.finderskill.client.BasicAuthClient;
import com.snowball.finder.finderskill.service.journey.JourneyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class AlternatePublicJourneyIntentHandler implements RequestHandler {

    static final Logger logger = LoggerFactory.getLogger(AlternatePublicJourneyIntentHandler.class);
    private BasicAuthClient client;
    private Properties prop;

    public AlternatePublicJourneyIntentHandler(BasicAuthClient client, Properties prop) {
        this.client = client;
        this.prop = prop;
    }

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName(IntentConstant.INTENT_ALT_PUBLIC_JOURNEY));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();
        String from = attributes.get(JourneyService.ATTR_FROM).toString();
        String to = attributes.get(JourneyService.ATTR_TO).toString();
        int routeNo = 0;
        if (!isValidAttribute(from) || !isValidAttribute(to)) {
            return respondNoCurrentJourney(input);
        }

        /* Handle return to first journey */
        String returnToFirst = getReturnToFirst(input);
        if (returnToFirst != null && returnToFirst.equals(JourneyService.RETURN_TO_FIRST_JOURNEY_YES)) {
            /* Tell user that first journey is being processed */
            sendProgressiveResponse(input.getServiceClientFactory().getDirectiveService(), input.getRequestEnvelope().getRequest().getRequestId(), JourneyService.RETURN_TO_FIRST_JOURNEY_MSG);
        } else if (returnToFirst != null && returnToFirst.equals(JourneyService.RETURN_TO_FIRST_JOURNEY_NO)) {
            /* Send acknowledgement that session is ended. */
            return respondStopLookup(input);
        }
        else {
            /* Send progressive response to let the user know their request is being processed */
            sendProgressiveResponse(input.getServiceClientFactory().getDirectiveService(),
                    input.getRequestEnvelope().getRequest().getRequestId(), JourneyService.generateAlt1JourneyProgressiveText(new Random()));
            /* If route number found, increment it from the previous search */
            routeNo = Integer.valueOf(attributes.get(Optional.of(JourneyService.ATTR_ROUTE_NO).orElse("0")).toString());
            routeNo++;
        }

        from = JourneyService.resolveHomeAddress(from, input.getRequestEnvelope().getContext().getSystem().getUser().getPermissions(), input.getRequestEnvelope().getContext().getSystem().getDevice().getDeviceId(), input.getServiceClientFactory().getDeviceAddressService());
        to = JourneyService.resolveHomeAddress(to, input.getRequestEnvelope().getContext().getSystem().getUser().getPermissions(), input.getRequestEnvelope().getContext().getSystem().getDevice().getDeviceId(), input.getServiceClientFactory().getDeviceAddressService());
        String responseText = getJourneyText(from, to, routeNo);

        if (responseText.equals(JourneyService.NO_ALT_ROUTE_MSG)) {
            return respondNoRoutes(input, responseText);
        }
        saveSessionAttributes(input, from, to, routeNo, responseText);
        if (responseText.equals(JourneyService.ALL_ROUTES_USED_MSG)) {
            return respondAllRoutesUsed(input, responseText);
        }
        return input.getResponseBuilder()
                .withSpeech(responseText)
                .withSimpleCard("JourneySession", responseText)
                .build();
    }

    private String getReturnToFirst(HandlerInput input) {
        Map<String, Slot> slots = getSlots(input);
        return slots.get(JourneyService.RETURN_TO_FIRST_JOURNEY_SLOT).getValue();
    }

    private Optional<Response> respondStopLookup(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech(JourneyService.END_SESSION_MSG)
                .withSimpleCard("JourneySession", JourneyService.END_SESSION_MSG)
                .withShouldEndSession(true)
                .build();
    }

    private Optional<Response> respondNoCurrentJourney(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech(JourneyService.NO_CURRENT_JOURNEY_MSG)
                .withSimpleCard("JourneySession", JourneyService.NO_CURRENT_JOURNEY_MSG)
                .build();
    }

    private Optional<Response> respondNoRoutes(HandlerInput input, String responseText) {
        clearSessionAttributes(input);
        return input.getResponseBuilder()
                .withSpeech(responseText)
                .withSimpleCard("JourneySession", responseText)
                .withShouldEndSession(true)
                .build();
    }

    private Optional<Response> respondAllRoutesUsed(HandlerInput input, String responseText) {
        IntentRequest intentRequest = (IntentRequest) input.getRequestEnvelope().getRequest();
        return input.getResponseBuilder()
                .withSpeech(responseText)
                .withSimpleCard("JourneySession", responseText)
                .addElicitSlotDirective(JourneyService.RETURN_TO_FIRST_JOURNEY_SLOT, intentRequest.getIntent())
                .build();
    }

    private String getJourneyText(String from, String to, int routeNo) {
        return JourneyService.getJourneyText(from, to, routeNo,
                client,
                prop.getProperty("transportApi.endpoint") + JourneyService.PUBLIC_JOURNEY_ENDPOINT,
                prop.getProperty("transportApi.username"),
                prop.getProperty("transportApi.password"), logger);
    }

    private boolean isValidAttribute(String attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return false;
        }
        return true;
    }

    private void saveSessionAttributes(HandlerInput input, String from, String to, Integer routeNo, String responseText) {
        Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();
        attributes.put(JourneyService.ATTR_FROM, from);
        attributes.put(JourneyService.ATTR_TO, to);
        attributes.put(JourneyService.ATTR_ROUTE_NO, routeNo.toString());
        attributes.put(JourneyService.ATTR_LAST_JOURNEY, responseText);
        input.getAttributesManager().setSessionAttributes(attributes);
    }

    private void clearSessionAttributes(HandlerInput input) {
        Map<String, Object> attributes = new HashMap<>();
        input.getAttributesManager().setSessionAttributes(attributes);
    }

    private void sendProgressiveResponse(DirectiveService directiveService, String requestId, String speech) {
        try {
            Header header = Header.builder().withRequestId(requestId).build();
            SpeakDirective directive = SpeakDirective.builder().withSpeech(speech).build();
            SendDirectiveRequest dirRequest = SendDirectiveRequest.builder().withHeader(header).withDirective(directive).build();
            directiveService.enqueue(dirRequest);
        } catch (ServiceException e) {
            /* This happens when running functional tests on AWS Lambda */
            logger.error("Failed to send progressive response", e);
        }
    }

    private Map<String, Slot> getSlots(HandlerInput input) {
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        return intent.getSlots();
    }

}
