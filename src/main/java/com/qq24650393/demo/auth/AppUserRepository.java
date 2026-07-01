package com.qq24650393.demo.auth;

import java.util.Optional;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AppUserRepository {

    @Select("""
            select id, username, password_hash, roles, enabled, created_at, updated_at
            from app_users
            where username = #{username}
            """)
    Optional<AppUser> findByUsername(String username);

    @Insert("""
            insert into app_users (username, password_hash, roles, enabled, created_at, updated_at)
            values (#{username}, #{passwordHash}, #{roles}, #{enabled}, now(6), now(6))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AppUser user);
}
