package com.exemple.service.resource.login.dao;

import java.util.UUID;
import java.util.stream.Stream;

import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Delete;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Query;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.exemple.service.resource.login.model.LoginEntity;

@Dao
public interface LoginDao {

    @Select
    LoginEntity findByUsername(String username);

    @Query("SELECT * FROM ${keyspaceId}.login WHERE id = :id")
    Stream<LoginEntity> findById(UUID id);

    @Insert(ifNotExists = true)
    boolean create(LoginEntity login);

    @Delete(entityClass = LoginEntity.class)
    void deleteByUsername(String username);

}
