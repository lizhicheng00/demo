package com.qq24650393.demo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "listening_configs")
public class ListeningConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "relay_domain_id", nullable = false)
    private RelayDomain relayDomain;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "node_id")
    private Node node;

    @Column(name = "listen_port", nullable = false)
    private int listenPort;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ListeningProtocol protocol;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ListeningStatus status = ListeningStatus.ENABLED;

    @Column(nullable = false)
    private long version = 1L;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        version++;
    }

    public Long getId() {
        return id;
    }

    public RelayDomain getRelayDomain() {
        return relayDomain;
    }

    public void setRelayDomain(RelayDomain relayDomain) {
        this.relayDomain = relayDomain;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
