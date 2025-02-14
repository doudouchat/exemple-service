package com.exemple.service.resource.account.model;

import java.util.UUID;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import com.exemple.service.resource.common.history.HistoryModel;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@Entity
@CqlName("account_history")
@ToString(callSuper = true)
public class AccountHistory extends HistoryModel<UUID> {

    @PartitionKey
    private UUID id;

}
