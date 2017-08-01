package me.falconpenny.portscanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import me.falconpenny.portscanner.data.*;
import me.falconpenny.portscanner.threads.LoggingThread;
import me.falconpenny.portscanner.threads.PollThread;
import me.falconpenny.portscanner.threads.PortIterationThread;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

        new PortIterationThread().start();
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
        System.out.println(" => [" + current++ + "] PORT RANGE");
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
                // SERVER LIST
                Utilities.clearTerminal();
                System.out.println(" => ENTER NEW (" + config.getServerFile() + ')');
                System.out.print(" -> ");
                final Pattern pFile = Pattern.compile("[0-9a-z]+\\.(csv|json|txt)", Pattern.CASE_INSENSITIVE);
                while (!scanner.hasNext(pFile)) {
                } // Wait until string matches.
                config.setServerFile(scanner.next());
                break;
            case 7:
                // PORT RANGE
                portMenu(scanner);
                break;
            case 6:
                // RETURN
                mainMenu(scanner);
                return;
            default:
                throw new IllegalStateException("Input cannot be default!");
        }
        configMenu(scanner);
    }

    private void portMenu(Scanner scanner) {
        Utilities.clearTerminal();
        int current = 1;
        System.out.println(" => [" + current++ + "] PORT LIST (" + config.getPorts().size() + " RANGES)");
        System.out.println(" => [" + current++ + "] ADD PORT/- RANGE");
        System.out.println(" => [" + current++ + "] RETURN");
        int in;
        while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > --current || in < 1)) {
            // Wait for proper int
        }
        switch (in) {
            case 1:
                // PORT LIST
                portListMenu(scanner);
                break;
            case 2:
                portListMenuAdd(scanner);
                break;
            case 3:
                // RETURN TO CONFIG
                return;
            default:
                throw new IllegalStateException("Input cannot be default!");
        }
        portMenu(scanner);
    }

    private void portListMenuAdd(Scanner scanner) {
        Utilities.clearTerminal();
        System.out.println(" => [1-65535] ENTER LOWER BOUND (INCLUSIVE)");
        System.out.print(" -> ");
        int in;
        while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > 65535)) {
            // Wait for proper int
        }
        if (in <= 0) {
            return;
        }
        int lower = in;
        Utilities.clearTerminal();
        System.out.println(" => LOWER BOUND: " + lower);
        System.out.println(" => [1-65535] ENTER UPPER BOUND OR 0 (INCLUSIVE)");
        System.out.println(" => 0 = SINGLE PORT");
        while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > 65535)) {
            // Wait for proper int
        }
        int upper = in == 0 ? lower : in;
        IPort add;
        if (upper == lower) {
            add = new SinglePort(lower);
        } else {
            add = new PortRange(Math.min(lower, upper), Math.max(lower, upper));
        }
        config.getPorts().add(add);
        // RETURN TO PORT MENU
    }

    private void portListMenu(Scanner scanner) {
        Utilities.clearTerminal();
        Map<Integer, IPort> portMap = new HashMap<>();
        int current = 1;
        System.out.println(" => TOTAL: " + config.getPorts().size() + " RANGES");
        System.out.println(" => [0] RETURN");
        for (IPort port : config.getPorts()) {
            portMap.put(current, port);
            if (port instanceof SinglePort) {
                System.out.println(" => [" + current++ + "] " + ((SinglePort) port).getPort());
                continue;
            }
            System.out.println(" => [" + current++ + "] " + ((PortRange) port).getLowerBound() + " - " + ((PortRange) port).getUpperBound());
        }
        int in;
        while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > --current)) {
            // Wait for proper int
        }
        if (in <= 0) {
            // RETURN
            return;
        }
        Iterator<Map.Entry<Integer, IPort>> iterator = portMap.entrySet().iterator();
        for (int i = 1; i < in && iterator.hasNext(); i++) {
            iterator.next();
        }
        portListMenuHandle(scanner, iterator);
        portListMenu(scanner);
    }

    private void portListMenuHandle(Scanner scanner, Iterator<Map.Entry<Integer, IPort>> portIterator) {
        if (!portIterator.hasNext()) return;
        int current = 1;
        Map.Entry<Integer, IPort> entry = portIterator.next();
        System.out.print(" => PORT " + (entry.getValue() instanceof PortRange ? "RANGE " : "") + ": ");
        if (entry.getValue() instanceof PortRange) {
            System.out.println(((PortRange) entry.getValue()).getLowerBound() + " - " + ((PortRange) entry.getValue()).getUpperBound());
        } else {
            System.out.println(((SinglePort) entry.getValue()).getPort());
        }
        System.out.println(" => [" + current++ + "] RETURN");
        System.out.println(" => [" + current++ + "] DELETE");
        int in;
        while (!scanner.hasNextInt() || ((in = scanner.nextInt()) > --current)) {
            // Wait for proper int
        }
        switch (in) {
            case 1:
                return;
            case 2:
                portIterator.remove();
                return;
            default:
                throw new IllegalStateException("Input cannot be default!");
        }
    }

    public static void main(String[] args) {
        main.run();
    }
}
