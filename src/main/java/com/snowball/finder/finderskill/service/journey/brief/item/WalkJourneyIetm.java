package com.snowball.finder.finderskill.service.journey.brief.item;

import com.snowball.location.transport_api.response.RoutePart;
import com.google.common.base.Strings;

public class WalkJourneyIetm extends AbstractJourneyItem implements JourneyItem {

    RoutePart routePart;

    public WalkJourneyIetm(RoutePart routePart) {
        this.routePart = routePart;
    }

    protected String getSpeechText() {
        return new StringBuilder("Walk for ").append(formatTimestamp(routePart.getDuration()))
                .append(" to ").append(speechAddress(routePart.getTo())).append(". ").toString();
    }

    public String getSpeechText(String preamble) {
        if (Strings.isNullOrEmpty(preamble)) return getSpeechText();
        return new StringBuilder(preamble).append(", walk for ")
                .append(formatTimestamp(routePart.getDuration())).append(" to ")
                .append(speechAddress(routePart.getTo())).append(". ").toString();
    }
}
