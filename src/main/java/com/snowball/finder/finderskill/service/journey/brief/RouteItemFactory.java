package com.snowball.finder.finderskill.service.journey.brief;

import com.snowball.finder.finderskill.service.journey.brief.item.*;
import com.snowball.location.transport_api.response.RoutePart;

public class RouteItemFactory {

    public static JourneyItem getRouteItem(RoutePart routePart) {
        switch(routePart.getMode()) {
            case RoutePart.MODE_TRAIN:
                return new TrainJourneyItem(routePart);
            case RoutePart.MODE_BUS:
                return new BusJourneyItem(routePart);
            case RoutePart.MODE_DLR:
                return new DLRJourneyItem(routePart);
            case RoutePart.MODE_FOOT:
                return new WalkJourneyIetm(routePart);
            case RoutePart.MODE_TUBE:
                return new TubeJourneyItem(routePart);
            case RoutePart.MODE_WAIT:
                return new WaitJourneyItem(routePart);
            case RoutePart.MODE_UNKNOWN:
            case RoutePart.MODE_BOAT:
            case RoutePart.MODE_TRAM:
            case RoutePart.MODE_OVERGROUND:
            default:
                return new UnknownJourneyItem(routePart);
        }
    }
}
