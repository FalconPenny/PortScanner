package me.falconpenny.portscanner.data;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class Configuration {
    private int threads = 16;
    private int timeout = 3000;
    private LogType writePorts = LogType.JSON;
    private String serverFile = "servers.txt";
    private Set<IPort> ports = new HashSet<IPort>() {{
        add(new SinglePort(3306));
        add(new PortRange(8080, 8081));
        add(new PortRange(25500, 26000));
    }};
}
