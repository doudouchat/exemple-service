package com.exemple.service.resource.core.dao;

import com.datastax.oss.driver.api.core.PagingIterable;
import com.datastax.oss.driver.api.mapper.annotations.Dao;
import com.datastax.oss.driver.api.mapper.annotations.Insert;
import com.datastax.oss.driver.api.mapper.annotations.Select;
import com.datastax.oss.driver.api.mapper.annotations.Update;
import com.datastax.oss.driver.api.mapper.entity.saving.NullSavingStrategy;
import com.exemple.service.resource.schema.model.ResourceSchema;

@Dao
public interface ResourceSchemaDao {

    @Select
    ResourceSchema findByApplicationAndResourceAndVersion(String application, String resource, String version);

    @Select
    PagingIterable<ResourceSchema> findByApplication(String application);

    @Insert
    void create(ResourceSchema resourceSchema);

    @Update(nullSavingStrategy = NullSavingStrategy.SET_TO_NULL)
    void update(ResourceSchema resourceSchema);
}
