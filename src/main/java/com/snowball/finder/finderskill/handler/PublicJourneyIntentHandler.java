package com.snowball.finder.finderskill.handler;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.dispatcher.request.handler.RequestHandler;
import com.amazon.ask.model.*;
import com.amazon.ask.model.services.ServiceException;
import com.amazon.ask.model.services.directive.*;
import com.amazon.ask.request.Predicates;
import com.snowball.finder.finderskill.client.BasicAuthClient;
import com.snowball.finder.finderskill.service.journey.JourneyService;
import com.snowball.location.transport_api.response.PublicJourneyContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PublicJourneyIntentHandler implements RequestHandler {

    static final Logger logger = LoggerFactory.getLogger(PublicJourneyContainer.class);
    private BasicAuthClient client;
    private Properties prop;

    public PublicJourneyIntentHandler(BasicAuthClient client, Properties prop) {
        this.client = client;
        this.prop = prop;
    }

    @Override
    public boolean canHandle(HandlerInput input) {
        return input.matches(Predicates.intentName(IntentConstant.INTENT_PUBLIC_JOURNEY));
    }

    @Override
    public Optional<Response> handle(HandlerInput input) {
        // Load and save persistent attributes. Not ued yet but planned in a future release.
        /*
        try {
            Map<String, Object> persistentAttributes = getPersistentAttributes(input);
            persistentAttributes.put("foo", "bar");
            input.getAttributesManager().savePersistentAttributes();
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
         */
        /* Send progressive response to let the user know their request is being processed */
        sendProgressiveResponse(input.getServiceClientFactory().getDirectiveService(), input.getRequestEnvelope().getRequest().getRequestId());

        Map<String, Object> attributes = input.getAttributesManager().getSessionAttributes();
        Map<String, Slot> slots = getSlots(input);

        if (isAddressPermissionRequired(slots, input.getRequestEnvelope().getContext().getSystem().getUser().getPermissions())) {
            return sendAddressPermRequest(input, JourneyService.ADDRESS_PERMS_MSG);
        }
        String from = JourneyService.resolveHomeAddress(slots.get(JourneyService.FROM_SLOT).getValue(), input.getRequestEnvelope().getContext().getSystem().getUser().getPermissions(), input.getRequestEnvelope().getContext().getSystem().getDevice().getDeviceId(), input.getServiceClientFactory().getDeviceAddressService());
        String to = JourneyService.resolveHomeAddress(slots.get(JourneyService.TO_SLOT).getValue(), input.getRequestEnvelope().getContext().getSystem().getUser().getPermissions(), input.getRequestEnvelope().getContext().getSystem().getDevice().getDeviceId(), input.getServiceClientFactory().getDeviceAddressService());
        String responseText = getJourneyText(from, to);
        saveSessionAttributes(input, attributes, slots, responseText);

        return input.getResponseBuilder()
                .withSpeech(responseText)
                .withSimpleCard("JourneySession", responseText)
                .build();
    }

    private static Optional<Response> sendAddressPermRequest(HandlerInput input, String speechText) {
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withReprompt(speechText)
                .withAskForPermissionsConsentCard(Arrays.asList(JourneyService.ADDRESS_PERMISSIONS))
                .build();
    }

    private static Map<String, Object> getPersistentAttributes(HandlerInput input) {
        return input.getAttributesManager().getPersistentAttributes();
    }

    private static void sendProgressiveResponse(DirectiveService directiveService, String requestId) {
        try {
            Header header = Header.builder().withRequestId(requestId).build();
            SpeakDirective directive = SpeakDirective.builder().withSpeech(JourneyService.generateJourneyProgressiveText(new Random())).build();
            SendDirectiveRequest dirRequest = SendDirectiveRequest.builder().withHeader(header).withDirective(directive).build();
            directiveService.enqueue(dirRequest);
        } catch (ServiceException e) {
            /* This happens when running functional tests on AWS Lambda */
            logger.error("Failed to send progressive response", e);
        }
    }

    private static Map<String, Slot> getSlots(HandlerInput input) {
        Request request = input.getRequestEnvelope().getRequest();
        IntentRequest intentRequest = (IntentRequest) request;
        Intent intent = intentRequest.getIntent();
        return intent.getSlots();
    }

    private String getJourneyText(String from, String to) {
        return JourneyService.getJourneyText(from, to, client,
                prop.getProperty("transportApi.endpoint") + JourneyService.PUBLIC_JOURNEY_ENDPOINT,
                prop.getProperty("transportApi.username"),
                prop.getProperty("transportApi.password"), logger);
    }

    private static void saveSessionAttributes(HandlerInput input, Map<String, Object> attributes, Map<String, Slot> slots, String responseText) {
        attributes.put(JourneyService.ATTR_FROM, slots.get(JourneyService.FROM_SLOT).getValue());
        attributes.put(JourneyService.ATTR_TO, slots.get(JourneyService.TO_SLOT).getValue());
        attributes.put(JourneyService.ATTR_LAST_JOURNEY, responseText);
        input.getAttributesManager().setSessionAttributes(attributes);
    }

    private static boolean isAddressPermissionRequired(Map<String, Slot> slots, Permissions permissions) {
        return ((JourneyService.isLookingForHome(slots.get(JourneyService.FROM_SLOT).getValue()) || JourneyService.isLookingForHome(slots.get(JourneyService.TO_SLOT).getValue()))
                && !JourneyService.hasPermissions(permissions));
    }

}
