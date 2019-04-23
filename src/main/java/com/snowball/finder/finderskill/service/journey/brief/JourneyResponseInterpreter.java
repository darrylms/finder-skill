package com.snowball.finder.finderskill.service.journey.brief;

import com.snowball.finder.finderskill.exception.AllRoutesUsedException;
import com.snowball.finder.finderskill.exception.EmptyJourneyException;
import com.snowball.finder.finderskill.exception.NoRouteException;
import com.snowball.finder.finderskill.service.journey.ConcatGenerator;
import com.snowball.finder.finderskill.service.journey.brief.item.AbstractJourneyItem;
import com.snowball.location.transport_api.response.PublicJourneyContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class JourneyResponseInterpreter {

    public static final String NEXT = "Next";
    public static final String THEN = "Then";
    public static final String FROM_THERE = "From there";
    public static final String AFTER_THAT = "After that";
    public static final String THEN_AFTER_THAT = "Then after that";
    public static final String THEREAFTER = "Thereafter";
    public static final String AND_THEN = "And then";
    public static final List<String> CONCAT = Collections.unmodifiableList(Arrays.asList(NEXT, THEN, FROM_THERE, AFTER_THAT, THEN_AFTER_THAT, THEREAFTER, AND_THEN));

    public static String interpret(PublicJourneyContainer journeyContainer, int routeNo) throws EmptyJourneyException, NoRouteException, AllRoutesUsedException {
        if (journeyContainer.getRoutes().isEmpty()) {
            throw new EmptyJourneyException("No routes found in journey");
        }
        if (journeyContainer.getRoutes().size() < routeNo + 1 && journeyContainer.getRoutes().size() > 1) {
                throw new AllRoutesUsedException("Requested route number is higher than the number of available routes.");
        }
        if (journeyContainer.getRoutes().size() < routeNo + 1) {
            throw new NoRouteException("No alternate routes available for this journey.");
        }
        String journeyHeader = "This journey takes " + AbstractJourneyItem.formatTimestamp(journeyContainer.getRoutes().get(0).getDuration()) + "<break time=\"370ms\"/>. ";
        final ConcatGenerator concat = ConcatGenerator.builder().options(CONCAT).random(new Random()).build();
        return journeyHeader + "<p><s>" +
                journeyContainer.getRoutes().get(routeNo).getRouteParts().stream()
                        .map(part -> RouteItemFactory.getRouteItem(part).getSpeechText(concat.next()))
                        .collect(Collectors.joining("</s><s>"))
                + "</s></p>";
    }
}
