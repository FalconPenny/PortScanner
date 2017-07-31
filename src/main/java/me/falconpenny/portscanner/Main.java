package me.falconpenny.portscanner;

import lombok.Getter;

public class Main {
    @Getter private static final Main main = new Main();
    @Getter private final VolatileStorage storage = new VolatileStorage();

    private void run() {
    }

    public static void main(String[] args) {

    }
}
