package pl.shajen.octopus.models;

import android.support.annotation.NonNull;

import java.util.Comparator;

public class Device implements Comparator<Device>, Comparable<Device> {
    private final String m_ip;
    private final String m_controller;

    public Device(String ip, String controller) {
        this.m_ip = ip;
        this.m_controller = controller;
    }

    public Device(String packedDevice) {
        String[] records = packedDevice.split(":");
        this.m_ip = records[0];
        this.m_controller = records[1];
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", m_controller, m_ip);
    }

    public String toPackedString() {
        return m_ip + ":" + m_controller;
    }

    public String ip() {
        return m_ip;
    }

    public String controller() {
        return m_controller;
    }

    @Override
    public int compareTo(@NonNull Device o) {
        return m_ip.compareTo(o.m_ip);
    }

    @Override
    public int compare(Device o1, Device o2) {
        return o1.m_ip.compareTo(o2.m_ip);
    }
}
