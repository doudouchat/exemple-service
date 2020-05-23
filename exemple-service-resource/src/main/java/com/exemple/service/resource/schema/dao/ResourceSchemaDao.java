package com.exemple.service.resource.schema.dao;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy;
import com.exemple.service.resource.schema.model.SchemaEntity;

@Dao
public interface ResourceSchemaDao {

    @Select
    SchemaEntity findByApplicationAndResourceAndVersionAndProfile(String application, String resource, String version, String profile);

    @Select
    PagingIterable<SchemaEntity> findByApplication(String application);

    @Insert
    void create(SchemaEntity resourceSchema);

    @Update(nullSavingStrategy = NullSavingStrategy.SET_TO_NULL)
    void update(SchemaEntity resourceSchema);
}
