package com.snowball.finder.finderskill.service.journey.brief.item;

import com.snowball.location.transport_api.response.RoutePart;
import com.google.common.base.Strings;

public class BusJourneyItem extends AbstractJourneyItem implements JourneyItem {

    RoutePart routePart;

    public BusJourneyItem(RoutePart routePart) {
        this.routePart = routePart;
    }

    protected String getSpeechText() {
        return new StringBuilder("Take the ").append(formatBusName(routePart.getLineName())).append(" bus from ")
                .append(speechAddress(routePart.getFrom())).append(" to ")
                .append(speechAddress(routePart.getTo())).append(". ").toString();
    }

    public String getSpeechText(String preamble) {
        if (Strings.isNullOrEmpty(preamble)) return getSpeechText();
        return new StringBuilder(preamble).append(", take the ").append(formatBusName(routePart.getLineName())).append(" bus to ")
                .append(speechAddress(routePart.getTo())).append(". ").toString();
    }

    /**
     * Format bus name to sound a little more natural.
     * Bus names from 1-99 are formatted as cardinal numbers, i.e. 55 will be pronounced as fifty-five.
     * All other bus names will be rendered as individual characters (See-three, two-seven-seven) unless they are
     * unusually formed, e.g. 477|Shoppers Heaven, in which case the preceding numbers will be rendered separately from the rest of the text.
     * @param busName
     * @return String
     */
    public String formatBusName(String busName) {
        busName = sanitise(busName);
        if (busName.matches("^(100|[1-9][0-9]?)$")) {
            return speechNumber(busName);
        }
        /* TODO: Find out why \p{javaSpaceChar} doesn't work */
//        if (busName.length() > 5 && busName.matches(WHITESPACE)) {
        if (busName.length() > 5 && busName.contains(" ")) {
            String[] parts = busName.split(" ");
            return speechDigits(parts[0]) + " " + busName.substring(parts[0].length() + 1);
        }
        if (busName.length() > 5) {
            return busName;
        }
        return speechCharacters(busName);
    }
}
