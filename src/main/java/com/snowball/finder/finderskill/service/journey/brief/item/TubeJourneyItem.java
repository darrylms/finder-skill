package com.snowball.finder.finderskill.service.journey.brief.item;

import com.snowball.location.transport_api.response.RoutePart;
import com.google.common.base.Strings;

public class TubeJourneyItem extends AbstractJourneyItem  implements JourneyItem {

    RoutePart routePart;

    public TubeJourneyItem(RoutePart routePart) {
        this.routePart = routePart;
    }

    protected String getSpeechText() {
        return new StringBuilder("Take the ").append(routePart.getLineName()).append(" line to ")
                .append(routePart.getTo()).append(". ").toString();
    }

    public String getSpeechText(String preamble) {
        if (Strings.isNullOrEmpty(preamble)) return getSpeechText();
        return new StringBuilder(preamble).append(", take the ").append(routePart.getLineName()).append(" line to ")
                .append(routePart.getTo()).append(". ").toString();
    }

}
