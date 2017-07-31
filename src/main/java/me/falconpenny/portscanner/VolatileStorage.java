package me.falconpenny.portscanner;

import lombok.Getter;

import java.net.InetAddress;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class VolatileStorage {
    @Getter
    private final Queue<String> output = new LinkedBlockingQueue<>();
    @Getter
    private final Queue<Map.Entry<Map.Entry<String, InetAddress>, Integer>> ports = new ConcurrentLinkedQueue<>();
}
