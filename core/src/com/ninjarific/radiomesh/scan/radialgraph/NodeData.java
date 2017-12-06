package com.ninjarific.radiomesh.scan.radialgraph;

public class NodeData {
    public static NodeData ROOT_NODE = new NodeData("root", "root", 0, 0);

    private final String bssid;
    private final String ssid;
    private final int strength;
    private final int frequency;

    public NodeData(String bssid, String ssid, int strength, int frequency) {
        this.bssid = bssid;
        this.ssid = ssid;
        this.strength = strength;
        this.frequency = frequency;
    }

    public String getBssid() {
        return bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public int getStrength() {
        return strength;
    }

    public int getFrequency() {
        return frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NodeData)) return false;
        NodeData other = (NodeData) o;
        return bssid.equals(other.bssid) && ssid.equals(other.ssid) && frequency == other.frequency;
    }
}
