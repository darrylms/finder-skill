package com.snowball.finder.finderskill.service.journey.brief.item;

import com.google.common.base.Strings;
import com.snowball.location.transport_api.response.RoutePart;

public class TrainJourneyItem extends AbstractJourneyItem  implements JourneyItem {

    RoutePart routePart;

    public TrainJourneyItem(RoutePart routePart) {
        this.routePart = routePart;
    }

    protected String getSpeechText() {
        return new StringBuilder("From ").append(speechAddress(routePart.getFrom())).append(", take a train via National Rail to ")
                .append(speechAddress(routePart.getTo())).append(". ").toString();
    }

    public String getSpeechText(String preamble) {
        if (Strings.isNullOrEmpty(preamble)) return getSpeechText();
        return new StringBuilder(preamble).append(", take a National Rail train to ")
                .append(speechAddress(routePart.getTo())).append(". ").toString();
    }

}
