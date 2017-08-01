package me.falconpenny.portscanner.data;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Configuration {
    private int threads = 16;
    private int timeout = 3000;
    private LogType writePorts = LogType.JSON;
    private String serverFile = "servers.txt";
}
