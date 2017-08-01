package me.falconpenny.portscanner.threads;

import me.falconpenny.portscanner.Main;
import me.falconpenny.portscanner.Utilities;
import me.falconpenny.portscanner.data.Scan;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class PollThread extends Thread {
    @Override
    public void run() {
        while (!isInterrupted()) {
            Scan scan = Main.getMain().getStorage().getPorts().poll();
            if (scan == null) continue;
            Socket socket;
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(scan.getDomain(), scan.getPort()), Main.getMain().getConfig().getTimeout());
            } catch (IOException e) {
                continue;
            }
            if (socket.isClosed()) continue;
            scan.setAddress(socket.getInetAddress().getHostAddress());
            try {
                socket.close();
            } catch (IOException e) {
            }
            boolean isIp = scan.getDomain().equals(scan.getAddress());
            Utilities.log(true, "Port " + scan.getPort() + " at " + scan.getDomain() + (isIp ? "" : "(" + scan.getAddress() + ")") + ": " + scan.getPort());
        }
    }
}
