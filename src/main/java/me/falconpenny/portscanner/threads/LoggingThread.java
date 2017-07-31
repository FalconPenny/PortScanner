package me.falconpenny.portscanner.threads;

import lombok.Setter;
import me.falconpenny.portscanner.Main;

import java.time.format.DateTimeFormatter;
import java.util.GregorianCalendar;

public class LoggingThread extends Thread {
    @Setter private boolean stop = false;

    @Override
    public void run() {
        while (!stop) {
            String out = Main.getMain().getStorage().getOutput().poll();
            if (out != null) {
                System.out.println(new GregorianCalendar().toZonedDateTime().format(DateTimeFormatter.ISO_LOCAL_TIME) + " -> " + out);
            }
        }
    }
}
