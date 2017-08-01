package me.falconpenny.portscanner.data;

import lombok.Data;

@Data
public class Scan {
    private String address;
    private final String domain;
    private final int port;
}
