package me.falconpenny.portscanner.data;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class SinglePort implements IPort {
    private final int port;

    @Override
    public Set<Integer> getPorts() {
        return new HashSet<Integer>() {{
            add(port);
        }};
    }
}
