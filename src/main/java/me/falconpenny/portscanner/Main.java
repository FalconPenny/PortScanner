package me.falconpenny.portscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.falconpenny.portscanner.data.Configuration;
import me.falconpenny.portscanner.data.LogType;
import me.falconpenny.portscanner.data.VolatileStorage;
import me.falconpenny.portscanner.threads.LoggingThread;
import me.falconpenny.portscanner.threads.PollThread;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main {
    @Getter private static final Main main = new Main();
    @Getter private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().disableHtmlEscaping().create();
    @Getter private final VolatileStorage storage = new VolatileStorage();
    @Getter private Configuration config = new Configuration();

    private final LoggingThread loggingThread = new LoggingThread();
    private final Set<PollThread> threads = new HashSet<>();

    private void run() {
        loggingThread.start();
        Scanner scanner = new Scanner(System.in);
        File config = new File("config.json");
        if (config.exists()) {
            try (FileReader reader = new FileReader(config)) {
                Configuration configuration = gson.fromJson(reader, Configuration.class);
                this.config = configuration;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            scanner.close();
            threads.forEach(t -> {
                try {
                    t.interrupt();
                } catch (Throwable ignored) {
                }
            });
            loggingThread.setStop(true);
            try (FileWriter writer = new FileWriter(config)) {
                gson.toJson(this.config, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        try {
            mainMenu(scanner);
        } catch (IllegalStateException ex) {
            return;
        }
        String filename = this.config.getServerFile().toLowerCase();
        File serverfile = new File(this.config.getServerFile());
        if (filename.endsWith(".json")) {
            try(FileReader reader = new FileReader(serverfile)) {
                if (!reader.ready()) {
                    Utilities.log("No data in the server list!");
                    return;
                }
                int charAmount = 1;
                char[] initialChars = new char[charAmount];
                reader.read(initialChars, 0, charAmount);
                if (initialChars[0] == '{') {
                    // TODO: Parse map
                } else if (initialChars[0] == '[') {
                    // TODO: Parse list
                } else {
                    throw new UnsupportedEncodingException("Json type not recognized!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (filename.endsWith(".csv")) {
            // TODO: Parse CSV
        } else if (filename.endsWith(".txt")) {
            // TODO: Parse upon \n
        } else {
            throw new IllegalArgumentException("Illegal file type of server list! (" + filename.substring(filename.lastIndexOf('.')) + ')');
        }
        Utilities.clearTerminal();
        Stream<PollThread> threadStream = IntStream.rangeClosed(0, this.config.getThreads()).mapToObj(i -> new PollThread());
        threadStream.forEach(threads::add);
        threadStream.forEach(Thread::start);


        // TODO: Implement console
    }

    private void mainMenu(Scanner scanner) {
        Utilities.clearTerminal();
        int current = 1;
        System.out.println(" => [" + current++ + "] CONFIG");
        System.out.println(" => [" + current++ + "] START");
        System.out.println(" => [" + current++ + "] EXIT");
        System.out.print(" -> ");
        int in;
        while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > --current || in < 1)) {
            // Wait for proper int
        }
        switch (in) {
            case 1:
                configMenu(scanner);
                return;
            case 2:
                // Move on to start the program.
                return;
            case 3:
                System.exit(0);
                throw new IllegalStateException("System exit!");
            default:
                throw new IllegalStateException("Input cannot be default!");
        }
    }

    private void configMenu(Scanner scanner) {
        Utilities.clearTerminal();
        int current = 1;
        System.out.println(" => [" + current++ + "] THREADS (" + config.getThreads() + ')');
        System.out.println(" => [" + current++ + "] TIMEOUT (" + config.getTimeout() + "ms)");
        System.out.println(" => [" + current++ + "] LOGGING (" + config.getWritePorts().name() + ')');
        System.out.println(" => [" + current++ + "] SERVER LIST (" + config.getServerFile() + ')');
        System.out.println(" => [" + current++ + "] RETURN");
        System.out.print(" -> ");
        int in;
        while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > --current || in < 1)) {
            // Wait for proper int
        }
        switch (in) {
            case 1:
                // THREADS
                Utilities.clearTerminal();
                System.out.println(" => [1-2048] ENTER NEW (" + config.getThreads() + ')');
                System.out.print(" -> ");
                while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > 2048 || in < 1)) {
                    // Wait for proper int
                }
                config.setThreads(in);
                break;
            case 2:
                // TIMEOUT
                Utilities.clearTerminal();
                System.out.println(" => [1-5000] ENTER NEW (" + config.getTimeout() + "ms)");
                System.out.print(" -> ");
                while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > 5000 || in < 1)) {
                    // Wait for proper int
                }
                config.setTimeout(in);
                break;
            case 3:
                // LOGGING
                Utilities.clearTerminal();
                current = 1;
                List<LogType> types = Arrays.asList(LogType.values());
                System.out.println(" => [" + (types.indexOf(config.getWritePorts()) + 1) + "] CURRENT: " + config.getWritePorts().name());
                for (LogType type : types) {
                    System.out.println(" => [" + current++ + "] " + type.name());
                }
                System.out.print(" -> ");
                while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > --current || in < 1)) {
                    // Wait for proper int
                }
                config.setWritePorts(types.get(--in));
                break;
            case 4:
                // TODO: LIST
                Utilities.clearTerminal();
                System.out.println(" => ENTER NEW (" + config.getServerFile() + ')');
                System.out.print(" -> ");
                final Pattern pFile = Pattern.compile("[0-9a-z]+\\.(csv|json|txt)", Pattern.CASE_INSENSITIVE);
                while (!scanner.hasNext(pFile)) {} // Wait until string matches.
                config.setServerFile(scanner.next());
                break;
            case 5:
                mainMenu(scanner);
                return;
            default:
                throw new IllegalStateException("Input cannot be default!");
        }
        configMenu(scanner);
    }

    public static void main(String[] args) {
        main.run();
    }
}
