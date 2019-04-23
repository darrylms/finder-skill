package com.snowball.finder.finderskill.service.journey.brief.item;

import java.time.LocalTime;

public abstract class AbstractJourneyItem {

    protected static String PIPE = "\\|";
    protected static String OPEN_BRACKET = "\\(";
    protected static String CLOSE_BRACKET = "\\)";
    protected static String SLASH = "\\/";
    protected static String WHITESPACE = "\\p{javaSpaceChar}";
    protected static String EXTRA_WHITESPACE = WHITESPACE + "+";

    public String speechAddress(String input) {
        return "<say-as interpret-as=\"address\">" + sanitise(input) + "</say-as>";
    }

    public String speechDigits(String input) {
        return "<say-as interpret-as=\"digits\">" + input + "</say-as>";
    }

    public String speechCharacters(String input) {
        return "<say-as interpret-as=\"characters\">" + input + "</say-as>";
    }

    public String speechNumber(String input) {
        return "<say-as interpret-as=\"number\">" + input + "</say-as>";
    }

    public static String formatTimestamp(String input) {
        LocalTime time = LocalTime.parse(input);
        String hour = formatTimeField(time.getHour(), "hour", "s");
        int increment = 0;
        // Round up for seconds
        if (time.getSecond() >= 30) increment++;
        String minute = formatTimeField(time.getMinute() + increment, "minute", "s");
        if (hour.isEmpty() && minute.isEmpty()) {
            return "less than a minute";
        }
        if (!hour.isEmpty()) hour += " and ";
        return hour + minute;
    }

    protected static String formatTimeField(Integer timeUnit, String unitName, String pluralise) {
        if (timeUnit == 0) {
            return "";
        }
        String formatted = timeUnit + " " + unitName;
        if (timeUnit > 1) formatted += pluralise;
        return formatted;
    }

    protected static String sanitise(String input) {
        return input.replaceAll(PIPE, " ")
                .replaceAll("[" + OPEN_BRACKET + "|" + CLOSE_BRACKET + "|" + SLASH + "]", "")
                .replaceAll(EXTRA_WHITESPACE + "+", " ")
                .trim();
    }
}
