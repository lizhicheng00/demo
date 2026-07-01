package com.qq24650393.demo.domain;

import java.time.Instant;

public class ListeningConfig {

    private Long id;
    private Long relayDomainId;
    private Long nodeId;
    private int listenPort;
    private ListeningProtocol protocol;
    private ListeningStatus status = ListeningStatus.ENABLED;
    private long version = 1L;
    private Instant createdAt;
    private Instant updatedAt;
    private String domain;
    private String targetUrl;
    private String nodeCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRelayDomainId() {
        return relayDomainId;
    }

    public void setRelayDomainId(Long relayDomainId) {
        this.relayDomainId = relayDomainId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public ListeningProtocol getProtocol() {
        return protocol;
    }

    public void setProtocol(ListeningProtocol protocol) {
        this.protocol = protocol;
    }

    public ListeningStatus getStatus() {
        return status;
    }

    public void setStatus(ListeningStatus status) {
        this.status = status;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }
}
