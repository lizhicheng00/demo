package com.qq24650393.demo.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NodeRepository extends JpaRepository<Node, Long> {

    Optional<Node> findByNodeCode(String nodeCode);

    long countByStatus(NodeStatus status);
}
