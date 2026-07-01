package com.qq24650393.demo.ops;

public class TrafficSummary {

    private long inboundBytes;
    private long outboundBytes;
    private long activeConnections;

    public long getInboundBytes() {
        return inboundBytes;
    }

    public void setInboundBytes(long inboundBytes) {
        this.inboundBytes = inboundBytes;
    }

    public long getOutboundBytes() {
        return outboundBytes;
    }

    public void setOutboundBytes(long outboundBytes) {
        this.outboundBytes = outboundBytes;
    }

    public long getActiveConnections() {
        return activeConnections;
    }

    public void setActiveConnections(long activeConnections) {
        this.activeConnections = activeConnections;
    }
}
