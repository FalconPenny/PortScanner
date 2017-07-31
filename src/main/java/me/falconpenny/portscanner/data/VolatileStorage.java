package me.falconpenny.portscanner.data;

import lombok.Getter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VolatileStorage {
    @Getter
    private final Queue<String> output = new LinkedBlockingQueue<>();
    @Getter
    private final Queue<Scan> ports = new ConcurrentLinkedQueue<>();
}
