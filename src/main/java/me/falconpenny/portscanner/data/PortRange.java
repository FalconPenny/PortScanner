package me.falconpenny.portscanner.data;

import lombok.Data;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class PortRange implements IPort {
    private final int lowerBound;
    private final int upperBound;

    @Override
    public Set<Integer> getPorts() {
        return IntStream.rangeClosed(lowerBound, upperBound).boxed().collect(Collectors.toSet());
    }
}
