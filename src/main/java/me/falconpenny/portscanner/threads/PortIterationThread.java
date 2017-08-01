package me.falconpenny.portscanner.threads;

import com.google.gson.Gson;
import me.falconpenny.portscanner.Main;
import me.falconpenny.portscanner.Utilities;
import me.falconpenny.portscanner.data.IPort;
import me.falconpenny.portscanner.data.Scan;
import me.falconpenny.portscanner.data.VolatileStorage;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PortIterationThread extends Thread {
    @Override
    public void run() {
        String filename = Main.getMain().getConfig().getServerFile();
        File serverfile = new File(filename);
        Gson gson = Main.getMain().getGson();
        VolatileStorage storage = Main.getMain().getStorage();
        Set<Integer> ports = new HashSet<>();
        for (IPort port : Main.getMain().getConfig().getPorts()) {
            ports.addAll(port.getPorts());
        }
        if (filename.endsWith(".json")) {
            try (FileReader reader = new FileReader(serverfile)) {
                if (!reader.ready()) {
                    Utilities.log("No data in the server list!");
                    return;
                }
                char initialChar = (char) reader.read();
                if (initialChar == '{') {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = gson.fromJson(reader, Map.class);
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        for (Integer port : ports)
                            storage.getPorts().offer(new Scan(entry.getKey(), port));
                    }
                } else if (initialChar == '[') {
                    @SuppressWarnings("unchecked")
                    List<String> map = gson.fromJson(reader, List.class);
                    for (String str : map) {
                        int indexOfColon = str.indexOf(':');
                        String ip = str.substring(0, indexOfColon == -1 ? str.length() : indexOfColon).replace(":", "");
                        for (Integer port : ports)
                            storage.getPorts().offer(new Scan(ip, port));
                    }
                } else {
                    throw new UnsupportedEncodingException("Json type not recognized!");
                }
            } catch (IOException e) {
                Utilities.log("Couldn't read from server list!");
                e.printStackTrace();
            }
        } else if (filename.endsWith(".csv")) {
            try {
                List<String> lines = Files.readAllLines(serverfile.toPath());
                for (String line : lines) {
                    boolean twocolumns = line.indexOf(',') != line.lastIndexOf(',');
                    String parse, ip;
                    if (twocolumns) {
                        parse = line.substring(0, line.length() - 1).replace(',', ':') + ',';
                    } else parse = line;
                    parse = parse.substring(0, line.length() - 1);
                    ip = parse.substring(0, line.indexOf(':')).replace(":", "");
                    for (Integer port : ports)
                        storage.getPorts().offer(new Scan(ip, port));
                }
            } catch (IOException e) {
                Utilities.log("Couldn't read from server list!");
                e.printStackTrace();
            }
        } else if (filename.endsWith(".txt")) {
            try {
                List<String> lines = Files.readAllLines(serverfile.toPath());
                for (String line : lines) {
                    String ip = line.substring(0, line.indexOf(':')).replace(":", "");
                    for (Integer port : ports)
                        storage.getPorts().offer(new Scan(ip, port));
                }
            } catch (IOException e) {
                Utilities.log("Couldn't read from server list!");
                e.printStackTrace();
            }
        } else {
            new ShutdownThread().start();
            throw new IllegalArgumentException("Illegal file type of server list! (" + filename.substring(filename.lastIndexOf('.')) + ')');
        }
    }
}
