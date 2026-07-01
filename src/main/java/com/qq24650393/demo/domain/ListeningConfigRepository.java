package com.qq24650393.demo.domain;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ListeningConfigRepository {

    @Select("select count(*) from listening_configs where status = #{status}")
    long countByStatus(ListeningStatus status);

    @Select("""
            select l.id, l.relay_domain_id, l.node_id, l.listen_port, l.protocol, l.status, l.version,
                   l.created_at, l.updated_at, d.domain, d.target_url, n.node_code
            from listening_configs l
            join relay_domains d on d.id = l.relay_domain_id
            left join nodes n on n.id = l.node_id
            where l.id = #{id}
            """)
    Optional<ListeningConfig> findById(Long id);

    @Select("""
            select l.id, l.relay_domain_id, l.node_id, l.listen_port, l.protocol, l.status, l.version,
                   l.created_at, l.updated_at, d.domain, d.target_url, n.node_code
            from listening_configs l
            join relay_domains d on d.id = l.relay_domain_id
            left join nodes n on n.id = l.node_id
            where l.status = #{status}
              and d.status = #{domainStatus}
              and (n.node_code = #{nodeCode} or n.id is null)
            order by l.version desc
            """)
    List<ListeningConfig> findSyncable(
            String nodeCode,
            ListeningStatus status,
            RelayDomainStatus domainStatus);

    @Insert("""
            insert into listening_configs
                (relay_domain_id, node_id, listen_port, protocol, status, version, created_at, updated_at)
            values
                (#{relayDomainId}, #{nodeId}, #{listenPort}, #{protocol}, #{status}, #{version}, now(6), now(6))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ListeningConfig config);

    @Update("""
            update listening_configs
            set status = #{status},
                version = version + 1,
                updated_at = now(6)
            where id = #{id}
            """)
    int updateStatus(ListeningConfig config);
}
