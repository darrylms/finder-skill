package com.snowball.finder.finderskill.service.journey.brief.item;

import com.snowball.location.transport_api.response.RoutePart;
import com.google.common.base.Strings;

public class UnknownJourneyItem extends AbstractJourneyItem  implements JourneyItem {

    RoutePart routePart;

    public UnknownJourneyItem(RoutePart routePart) {
        this.routePart = routePart;
    }

    protected String getSpeechText() {
        return new StringBuilder("From ").append(routePart.getFrom()).append(", travel by mystery machine to ")
                .append(routePart.getTo()).append(". ").toString();
    }

    public String getSpeechText(String preamble) {
        if (Strings.isNullOrEmpty(preamble)) return getSpeechText();
        return new StringBuilder(preamble).append(", travel by mystery machine to ")
                .append(routePart.getTo()).append(". ").toString();
    }
}
