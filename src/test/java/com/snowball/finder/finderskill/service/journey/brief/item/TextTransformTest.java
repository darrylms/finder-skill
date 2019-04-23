package com.snowball.finder.finderskill.service.journey.brief.item;

import com.snowball.location.transport_api.response.RoutePart;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static com.snowball.location.transport_api.response.RoutePart.MODE_BUS;

public class TextTransformTest {

    @Test
    public void testFormatBusName() {
        RoutePart route = new RoutePart();
        route.setMode(MODE_BUS);
        route.setArrival("16:35");
        route.setDestination("Green Street Green");
        route.setDuration("00:10:00");
        route.setFrom("High Street / Carlton Parade");
        route.setLineName("477|Shoppers Heaven");
        route.setTo("Orpington Station");
        BusJourneyItem journey = new BusJourneyItem(route);
        assertEquals(journey.formatBusName(route.getLineName()), "<say-as interpret-as=\"digits\">477</say-as> Shoppers Heaven");
    }

    @Test
    public void testSanitise() {
        String unsanitised = "477|Shoppers Heaven High Street / Carlton Parade (London)";
        String expected = "477 Shoppers Heaven High Street Carlton Parade London";

        String result = AbstractJourneyItem.sanitise(unsanitised);
        assertEquals(result, expected);;
    }

    @Test
    public void testFormatTimestamp() {
        assertEquals(AbstractJourneyItem.formatTimestamp("01:23:15"), "1 hour and 23 minutes");
        assertEquals(AbstractJourneyItem.formatTimestamp("02:45:51"), "2 hours and 46 minutes");
        assertEquals(AbstractJourneyItem.formatTimestamp("23:01:01"), "23 hours and 1 minute");
        assertEquals(AbstractJourneyItem.formatTimestamp("17:01:00"), "17 hours and 1 minute");
        assertEquals(AbstractJourneyItem.formatTimestamp("00:01:29"), "1 minute");
        assertEquals(AbstractJourneyItem.formatTimestamp("00:01:30"), "2 minutes");
        assertEquals(AbstractJourneyItem.formatTimestamp("00:59:59"), "60 minutes");
        assertEquals(AbstractJourneyItem.formatTimestamp("00:00:59"), "1 minute");
        assertEquals(AbstractJourneyItem.formatTimestamp("00:00:30"), "1 minute");
        assertEquals(AbstractJourneyItem.formatTimestamp("00:00:29"), "less than a minute");
    }

}
