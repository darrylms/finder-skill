package com.snowball.finder.finderskill.service.journey.brief.item;

import com.snowball.location.transport_api.response.RoutePart;
import com.google.common.base.Strings;

public class WaitJourneyItem extends AbstractJourneyItem  implements JourneyItem{
    RoutePart routePart;

    public WaitJourneyItem(RoutePart routePart) {
        this.routePart = routePart;
    }

    protected String getSpeechText() {
        return new StringBuilder("Wait for ").append(formatTimestamp(routePart.getDuration())).append(". ").toString();
    }

    public String getSpeechText(String preamble) {
        if (Strings.isNullOrEmpty(preamble)) return getSpeechText();
        return new StringBuilder(preamble).append(", wait for ").append(formatTimestamp(routePart.getDuration())).append(". ").toString();
    }

}
