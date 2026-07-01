package com.qq24650393.demo.domain;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RelayDomainRepository {

    @Select("select count(*) from relay_domains where domain = #{domain}")
    long countByDomain(String domain);

    @Select("""
            select id, domain, target_url, status, remark, created_at, updated_at
            from relay_domains
            where domain = #{domain}
            """)
    Optional<RelayDomain> findByDomain(String domain);

    @Select("""
            select id, domain, target_url, status, remark, created_at, updated_at
            from relay_domains
            where id = #{id}
            """)
    Optional<RelayDomain> findById(Long id);

    @Select("""
            select id, domain, target_url, status, remark, created_at, updated_at
            from relay_domains
            order by id desc
            """)
    List<RelayDomain> findAll();

    @Insert("""
            insert into relay_domains (domain, target_url, status, remark, created_at, updated_at)
            values (#{domain}, #{targetUrl}, #{status}, #{remark}, now(6), now(6))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RelayDomain relayDomain);

    @Update("""
            update relay_domains
            set domain = #{domain},
                target_url = #{targetUrl},
                remark = #{remark},
                updated_at = now(6)
            where id = #{id}
            """)
    int update(RelayDomain relayDomain);

    @Update("""
            update relay_domains
            set status = #{status},
                updated_at = now(6)
            where id = #{id}
            """)
    int updateStatus(RelayDomain relayDomain);

    @Delete("delete from relay_domains where id = #{id}")
    int deleteById(Long id);

    @Select("select count(*) from relay_domains")
    long count();

    @Select("select count(*) from relay_domains where status = #{status}")
    long countByStatus(RelayDomainStatus status);
}
