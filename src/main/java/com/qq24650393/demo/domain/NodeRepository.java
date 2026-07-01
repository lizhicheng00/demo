package com.qq24650393.demo.domain;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface NodeRepository {

    @Select("""
            select id, node_code, name, address, status, last_heartbeat_at, created_at, updated_at
            from nodes
            where node_code = #{nodeCode}
            """)
    Optional<Node> findByNodeCode(String nodeCode);

    @Select("""
            select id, node_code, name, address, status, last_heartbeat_at, created_at, updated_at
            from nodes
            order by id desc
            """)
    List<Node> findAll();

    @Insert("""
            insert into nodes (node_code, name, address, status, last_heartbeat_at, created_at, updated_at)
            values (#{nodeCode}, #{name}, #{address}, #{status}, #{lastHeartbeatAt}, now(6), now(6))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Node node);

    @Update("""
            update nodes
            set name = #{name},
                address = #{address},
                status = #{status},
                last_heartbeat_at = #{lastHeartbeatAt},
                updated_at = now(6)
            where id = #{id}
            """)
    int update(Node node);

    @Select("select count(*) from nodes")
    long count();

    @Select("select count(*) from nodes where status = #{status}")
    long countByStatus(NodeStatus status);
}
