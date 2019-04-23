package com.snowball.finder.finderskill.service.journey.brief.item;

import com.snowball.location.transport_api.response.RoutePart;
import com.google.common.base.Strings;

public class DLRJourneyItem extends AbstractJourneyItem  implements JourneyItem {

    RoutePart routePart;

    public DLRJourneyItem(RoutePart routePart) {
        this.routePart = routePart;
    }

    protected String getSpeechText() {
        return new StringBuilder("Take the ").append(speechCharacters("DLR")).append(" line to ").append(speechAddress(routePart.getTo())).append(". ").toString();
    }

    public String getSpeechText(String preamble) {
        if (Strings.isNullOrEmpty(preamble)) return getSpeechText();
        return new StringBuilder(preamble).append(", take the ").append(speechCharacters("DLR")).append(" line to ").append(speechAddress(routePart.getTo())).append(". ").toString();
    }
}
