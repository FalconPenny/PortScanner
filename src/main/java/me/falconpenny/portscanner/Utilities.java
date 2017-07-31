package me.falconpenny.portscanner;

import me.falconpenny.portscanner.data.LogType;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Utilities {
    private static AtomicReference<File> file = new AtomicReference<>();
    private static LogType typeLinked = Main.getMain().getConfig().getWritePorts();

    private Utilities() {
    }

    public static void log(boolean write, String... messages) {
        Stream<String> stream = Arrays.stream(messages);
        if (stream.count() == 0L) return;
        stream.forEach(Main.getMain().getStorage().getOutput()::offer);
        if (write) {
            if (typeLinked != Main.getMain().getConfig().getWritePorts()) {
                typeLinked = Main.getMain().getConfig().getWritePorts();
                if (typeLinked != LogType.OFF)
                    file.set(new File("output." + typeLinked.name()));
                else
                    file.set(null);
            }
            stream.forEach(typeLinked::write);
            typeLinked.flush();
        }
    }

    public static void log(String... messages) {
        log(false, messages);
        Arrays.stream(messages).forEach(Main.getMain().getStorage().getOutput()::offer);
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
