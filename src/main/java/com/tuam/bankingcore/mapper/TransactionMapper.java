package com.tuam.bankingcore.mapper;

import com.tuam.bankingcore.model.Transaction;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface TransactionMapper {

    @Insert("INSERT INTO transaction (account_id, amount, currency, direction, description, balance_after) " +
            "VALUES (#{accountId}, #{amount}, #{currency}, #{direction}, #{description}, #{balanceAfter})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Transaction transaction);

    @Select("SELECT id, account_id, amount, currency, direction, description, balance_after, created_at " +
        "FROM transaction WHERE account_id = #{accountId} ORDER BY created_at DESC")
    List<Transaction> findByAccountId(@Param("accountId") Long accountId);
}