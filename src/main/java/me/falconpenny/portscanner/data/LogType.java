package me.falconpenny.portscanner.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.falconpenny.portscanner.Main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Getter
public enum LogType {
    OFF(null) {
        @Override
        public void flush() {
            getFlush().clear();
        }
    },
    JSON("json") {
        @Override
        public void flush() {
            try {
                List<String> write = new ArrayList<>();
                if (getFile().get().exists()) {
                    FileReader reader = new FileReader(getFile().get());
                    //noinspection unchecked
                    write.addAll(Main.getMain().getGson().fromJson(reader, write.getClass()));
                    reader.close();
                    getFile().get().delete();
                }
                write.addAll(getFlush());
                getFlush().clear();
                FileWriter writer = new FileWriter(getFile().get());
                Main.getMain().getGson().toJson(write, writer);
                getFlush().clear();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    },
    CSV("csv") {
        @Override
        public void flush() {
            try (FileWriter writer = new FileWriter(getFile().get())) {
                getFlush().forEach(it -> {
                    try {
                        writer.append(it.replace("\"", "\"\"")).append(",\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            getFlush().clear();
        }
    },
    TXT("txt") {
        @Override
        public void flush() {
            try (FileWriter writer = new FileWriter(getFile().get())) {
                getFlush().forEach(it -> {
                    try {
                        writer.append(it).append(",\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
            getFlush().clear();
        }
    };

    private final String extension;
    private ArrayList<String> flush = new ArrayList<>();
    private AtomicReference<File> file = new AtomicReference<>();

    public void write(String in) {
        flush.add(in);
    }

    public abstract void flush();
}
