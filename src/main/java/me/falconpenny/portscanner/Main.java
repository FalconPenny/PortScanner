package me.falconpenny.portscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.falconpenny.portscanner.data.Configuration;
import me.falconpenny.portscanner.data.VolatileStorage;
import me.falconpenny.portscanner.threads.LoggingThread;
import me.falconpenny.portscanner.threads.PollThread;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    @Getter private static final Main main = new Main();
    @Getter
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    @Getter private final VolatileStorage storage = new VolatileStorage();
    @Getter private final Configuration config = new Configuration();

    private final LoggingThread loggingThread = new LoggingThread();
    private final Set<PollThread> threads = new HashSet<>();

    private void run() {
        loggingThread.start();
        Scanner scanner = new Scanner(System.in);

        int intSlot = 3;
        int current = 1;
        System.out.println('+' + Utilities.length("", 16, '-') + '+' + Utilities.length("", intSlot, '-') + '+');
        System.out.println('|' + Utilities.length(" CONFIG", 16, ' ') + '|' + Utilities.length(" " + current++, intSlot, ' ') + '|');
        System.out.println('|' + Utilities.length(" START", 16, ' ') + '|' + Utilities.length(" " + current++, intSlot, ' ') + '|');
        System.out.println('|' + Utilities.length(" EXIT", 16, ' ') + '|' + Utilities.length(" " + current++, intSlot, ' ') + '|');
        System.out.println('+' + Utilities.length("", 16, '-') + '+' + Utilities.length("", intSlot, '-') + '+');
        System.out.print("[1-" + current + "] -> ");
        int in;
        while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > current || in < 1)) {
            // Wait for proper int
        }
        switch (in) {
            case 1:
                // TODO: Configuration
                break;
            case 2:
                // Move on to start the program.
                break;
            case 3:
                System.exit(0);
                return;
            default:
                throw new IllegalStateException("Input cannot be default!");
        }
        Stream<PollThread> threadStream = IntStream.rangeClosed(0, config.getThreads()).mapToObj(i -> new PollThread());
        threadStream.forEach(threads::add);
        threadStream.forEach(Thread::start);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scanner.close();
            threads.forEach(t -> {
                try {
                    t.interrupt();
                } catch (Throwable ignored) {
                }
            });
            loggingThread.setStop(true);
        }));
    }

    public static void main(String[] args) {
        main.run();
    }
}
