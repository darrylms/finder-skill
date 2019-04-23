package com.snowball.finder.finderskill.service.journey;

import com.amazon.ask.model.Permissions;
import com.amazon.ask.model.services.deviceAddress.Address;
import com.amazon.ask.model.services.deviceAddress.DeviceAddressServiceClient;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.finder.finderskill.client.BasicAuthClient;
import com.snowball.finder.finderskill.exception.AllRoutesUsedException;
import com.snowball.finder.finderskill.exception.EmptyJourneyException;
import com.snowball.finder.finderskill.exception.NoRouteException;
import com.snowball.finder.finderskill.service.journey.brief.JourneyResponseInterpreter;
import com.snowball.location.transport_api.response.PublicJourneyContainer;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import java.util.*;

public class JourneyService {

    public static String ATTR_FROM = "from";
    public static String ATTR_TO = "to";
    public static String ATTR_LAST_JOURNEY = "last-journey";
    public static String ATTR_ROUTE_NO = "routeNo";
    public static String HOME = "home";

    public static String FROM_SLOT = "fromLocation";
    public static String TO_SLOT = "toLocation";
    public static String RETURN_TO_FIRST_JOURNEY_SLOT = "returnToFirst";
    public static String PUBLIC_JOURNEY_ENDPOINT = "/journey/public/{from}/{to}/";

    public static String RETURN_TO_FIRST_JOURNEY_YES = "yes";
    public static String RETURN_TO_FIRST_JOURNEY_NO = "no";

    public static String EMPTY_JOURNEY_MSG = "Sorry, an error occurred while retrieving your journey. Please try again.";
    public static String NO_ALT_ROUTE_MSG = "I could not find an alternate route for you.";
    public static String ALL_ROUTES_USED_MSG = "You have gone through all the available routes. Would you like to go back to the first one?";
    public static String NO_CURRENT_JOURNEY_MSG = "You have no current journey.";
    public static String RETURN_TO_FIRST_JOURNEY_MSG = "All right, looking up your first journey.";
    public static String END_SESSION_MSG = "Okay.";
    public static String NO_PREVIOUS_JOURNEY_MSG = "There is no previous journey for this session";

    public static List<String> JOURNEY_PROGRESSIVE_RESPONSE = Collections.unmodifiableList(Arrays.asList(
            "Okay, lets see what I can find for you.",
            "All right, I'll find a route for you.",
            "Great, taking a look.",
            "Great, I'll find a route for you.",
            "Looking up a route for you."
    ));
    public static List<String> ALT_JOURNEY_PROGRESSIVE_RESPONSE = Collections.unmodifiableList(Arrays.asList(
            "Okay, lets see what I can find for you.",
            "All right, I'll look for an alternate route.",
            "No problem. I'll see if there's another way.",
            "Looking for an alternate route.",
            "I'll see if I can find one for you."
    ));

    public static String ADDRESS_PERMISSIONS = "read::alexa:device:all:address";
    public static String ADDRESS_PERMS_MSG = "Please open your Alexa app and grant permissions to the Finder skill so that it can use this device's address";

    public static String getJourneyText(String from, String to, BasicAuthClient client, String endpoint, String username, String password, Logger logger) {
        return getJourneyText(from, to, 0, client, endpoint, username, password, logger);
    }

    public static String getJourneyText(String from, String to, int routeNo, BasicAuthClient client, String endpoint, String username, String password, Logger logger) {
        ImmutableMap<String, String> routeParams = ImmutableMap.of(
            "from", from,
            "to", to);
        try {
            HttpResponse<PublicJourneyContainer> response = client.getWithBasicAuth(
                    endpoint, null, routeParams,
                    username, password,
                    PublicJourneyContainer.class);
            PublicJourneyContainer journey = response.getBody();
            return JourneyResponseInterpreter.interpret(journey, routeNo);
        } catch (EmptyJourneyException | UnirestException e) {
            logger.error(e.getMessage(), e);
            return EMPTY_JOURNEY_MSG;
        } catch (NoRouteException e) {
            logger.error(e.getMessage(), e);
            return NO_ALT_ROUTE_MSG;
        } catch (AllRoutesUsedException e) {
            logger.error(e.getMessage(), e);
            return ALL_ROUTES_USED_MSG;
        }
    }

    public static String generateJourneyProgressiveText(Random random) {
        return JOURNEY_PROGRESSIVE_RESPONSE.get(random.nextInt(JOURNEY_PROGRESSIVE_RESPONSE.size()));
    }

    public static String generateAlt1JourneyProgressiveText(Random random) {
        return ALT_JOURNEY_PROGRESSIVE_RESPONSE.get(random.nextInt(ALT_JOURNEY_PROGRESSIVE_RESPONSE.size()));
    }

    /**
     * Attempt to look up home address if slot value equates to PublicJourneyIntentHandler.HOME.
     * @param slotValue Value of a single slot
     * @param permissions Alexa permissions object
     * @param deviceId Device ID
     * @param client DeviceAddressServiceClient provided by ServiceClientFactory
     * @return Lookup-ready home address or empty string if not found
     */
    public static String resolveHomeAddress(String slotValue, Permissions permissions, String deviceId, DeviceAddressServiceClient client) {
        if (isLookingForHome(slotValue)) {
            String homeAddress = getDeviceAddress(permissions, deviceId, client);
            return homeAddress;
        }
        return slotValue;
    }

    public static boolean isLookingForHome(String slotValue) {
        if (Strings.isNullOrEmpty(slotValue)) return false;
        return slotValue.toLowerCase().equals(JourneyService.HOME);
    }

    public static boolean hasPermissions(Permissions permissions) {
        return (permissions != null);
    }

    /**
     * Get the Alexa device's address.
     * NOTE: With full address permissions granted in the Alexa test suite, only the postcode is returned. Hopefully this is
     * a limitation of the test environment.
     * @param permissions Alexa permissions object
     * @param deviceId Device ID
     * @param addressServiceClient DeviceAddressServiceClient provided by ServiceClientFactory
     * @return String containing the Alexa device address, if available. If not, an empty String is returned.
     */
    private static String getDeviceAddress(Permissions permissions, String deviceId, DeviceAddressServiceClient addressServiceClient) {
        Address temp = addressServiceClient.getFullAddress(deviceId);
        return Optional.ofNullable(permissions)
                .flatMap(p -> Optional.ofNullable(addressServiceClient.getFullAddress(deviceId)))
                .map(address ->
                        getStringValue(address.getAddressLine1()) + " " +
                        getStringValue(address.getAddressLine2()) + " " +
                        getStringValue(address.getAddressLine3()) + " " +
                        getStringValue(address.getCity()) + " " +
                        getStringValue(address.getPostalCode())
                ).orElse("");
    }

    private static String getStringValue(String str) {
        if (Strings.isNullOrEmpty(str)) return "";
        return str.trim();
    }

}
