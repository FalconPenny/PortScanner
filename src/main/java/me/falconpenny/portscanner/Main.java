package me.falconpenny.portscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.falconpenny.portscanner.threads.LoggingThread;

import java.util.Scanner;

public class Main {
    @Getter private static final Main main = new Main();
    @Getter private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    @Getter private final VolatileStorage storage = new VolatileStorage();

    private final LoggingThread loggingThread = new LoggingThread();

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
        int in = -1;
        while (!scanner.hasNextInt() && ((in = scanner.nextInt()) > current || in < 1)) {
        }
        switch (in) {
            case 1:
                // TODO: Configuration
                break;
            case 2:
                // TODO: Start
                break;
            case 3:
                System.exit(0);
                return;
            default:
                break;
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scanner.close();
            loggingThread.setStop(true);
        }));
    }

    public static void main(String[] args) {
        main.run();
    }
}
