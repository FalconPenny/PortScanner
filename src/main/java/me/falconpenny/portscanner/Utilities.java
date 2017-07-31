package me.falconpenny.portscanner;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Utilities {
    private Utilities() {
    }

    public static void log(String... messages) {
        Arrays.stream(messages).forEach(Main.getMain().getStorage().getOutput()::add);
    }

    public static String length(String text, int length, char placeholder) {
        if (text.length() >= length) {
            return text;
        }
        StringBuilder builder = new StringBuilder();
        IntStream.rangeClosed(0, length).forEach(i -> builder.append(placeholder));
        return text + builder.toString();
    }
}
