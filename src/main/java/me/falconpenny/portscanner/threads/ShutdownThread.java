package me.falconpenny.portscanner.threads;

import me.falconpenny.portscanner.Main;

public class ShutdownThread extends Thread {
    @Override
    public void run() {
        while (Main.getMain().getStorage().getPorts().peek() != null || Main.getMain().getStorage().getOutput().peek() != null) {}
        System.exit(0);
    }
}
