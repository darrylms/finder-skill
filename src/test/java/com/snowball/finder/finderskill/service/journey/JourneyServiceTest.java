package com.snowball.finder.finderskill.service.journey;

import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.snowball.finder.finderskill.client.BasicAuthClient;
import com.snowball.location.transport_api.response.PublicJourneyContainer;
import com.snowball.location.transport_api.response.PublicJourneyRoute;
import com.snowball.location.transport_api.response.RoutePart;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import com.snowball.finder.finderskill.service.journey.brief.JourneyResponseInterpreter;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JourneyServiceTest {

    @Mock
    BasicAuthClient client;

    @Mock
    HttpResponse<PublicJourneyContainer> response;

    @Test
    void testGetJourneyText() throws UnirestException {
        PublicJourneyContainer journey = new PublicJourneyContainer();
        PublicJourneyRoute route = new PublicJourneyRoute();
        route.setDuration("01:23:00");

        RoutePart routeFoot = new RoutePart();
        routeFoot.setMode("foot");
        routeFoot.setDuration("00:05:23");
        routeFoot.setTo("Bus stop / With a slash");

        RoutePart routeBus = new RoutePart();
        routeBus.setMode("bus");
        routeBus.setLineName("477|Shoppers Heaven");
        routeBus.setFrom("Bus stop / With a slash");
        routeBus.setTo("A station");

        RoutePart routeTrain = new RoutePart();
        routeTrain.setMode("train");
        routeTrain.setFrom("A station");
        routeTrain.setTo("Another station");

        RoutePart routeDLR = new RoutePart();
        routeDLR.setMode("dlr");
        routeDLR.setFrom("Another station");
        routeDLR.setTo("Some tube station");

        RoutePart routeTube = new RoutePart();
        routeTube.setMode("tube");
        routeTube.setLineName("Test Line");
        routeTube.setTo("A place to wait");

        RoutePart routeWait = new RoutePart();
        routeWait.setMode("wait");
        routeWait.setDuration("00:03:31");

        RoutePart routeUnknown = new RoutePart();
        routeUnknown.setMode("unknown route");
        routeUnknown.setFrom("Current location");
        routeUnknown.setTo("Final destination");

        route.setRouteParts(Arrays.asList(routeFoot, routeBus, routeTrain, routeDLR, routeTube, routeWait, routeUnknown));
        journey.setRoutes(Arrays.asList(route));

        when(response.getBody()).thenReturn(journey);

        String from = "start location";
        String to = "destination";
        ImmutableMap<String, String> routeParams = ImmutableMap.of(
                "from", from,
                "to", to);
        Map<String, String> queryParams = null;
        String endpoint = "https://a.valid.endpoint/";
        String username = "user";
        String password = "pass";
        int routeNo = 0;

        when(client.getWithBasicAuth(endpoint, queryParams, routeParams, username, password, PublicJourneyContainer.class)).thenReturn(response);

        String speech = JourneyService.getJourneyText(from, to, routeNo, client, endpoint, username, password, LoggerFactory.getLogger(JourneyServiceTest.class));
        System.out.println(speech);

        assertTrue(!speech.isEmpty());

        //Split routes based on <s></s> tags
        Document doc = Jsoup.parse(speech);
        Elements nodes = doc.select("s");

        //Correct number of routes found in output speech
        assertEquals(nodes.size(), 7);

        //Skip the first element as it shouldn't have a preamble
        nodes.stream().skip(1).forEach(el -> {
            String line = el.html();
            String preamble = line.substring(0, line.indexOf(","));
            assertThat(JourneyResponseInterpreter.CONCAT, hasItem(preamble));
        });

        assertTrue(speech.startsWith("This journey takes 1 hour and 23 minutes"));
        assertTrue(speech.contains("<s>Walk for 5 minutes to <say-as interpret-as=\"address\">Bus stop With a slash</say-as>. </s>"));
        assertTrue(speech.contains(", take the <say-as interpret-as=\"digits\">477</say-as> Shoppers Heaven bus to <say-as interpret-as=\"address\">A station</say-as>. </s>"));
        assertTrue(speech.contains(", take a National Rail train to <say-as interpret-as=\"address\">Another station</say-as>. </s>"));
        assertTrue(speech.contains(", take the <say-as interpret-as=\"characters\">DLR</say-as> line to <say-as interpret-as=\"address\">Some tube station</say-as>. </s>"));
        assertTrue(speech.contains(", take the Test Line line to A place to wait. </s>"));
        assertTrue(speech.contains(", wait for 4 minutes. </s>"));
        assertTrue(speech.contains(", travel by mystery machine to Final destination. </s></p>"));
    }
}
