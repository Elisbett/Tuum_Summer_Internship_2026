package com.tuam.bankingcore.mapper;

import com.tuam.bankingcore.model.Balance;
import org.apache.ibatis.annotations.*;
import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BalanceMapper {

    @Insert("INSERT INTO balance (account_id, currency, amount) VALUES (#{accountId}, #{currency}, #{amount})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Balance balance);

    @Select("SELECT id, account_id as accountId, currency, amount, updated_at as updatedAt FROM balance WHERE account_id = #{accountId}")
    @Results(id = "balanceResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "accountId", column = "account_id"),
        @Result(property = "currency", column = "currency"),
        @Result(property = "amount", column = "amount"),
        @Result(property = "updatedAt", column = "updated_at")
    })
    List<Balance> findByAccountId(@Param("accountId") Long accountId);

    @Select("SELECT id, account_id as accountId, currency, amount, updated_at as updatedAt FROM balance WHERE account_id = #{accountId} AND currency = #{currency} FOR UPDATE")
    Balance findByAccountIdAndCurrencyForUpdate(@Param("accountId") Long accountId, @Param("currency") String currency);

    @Update("UPDATE balance SET amount = #{amount}, updated_at = CURRENT_TIMESTAMP WHERE account_id = #{accountId} AND currency = #{currency}")
    int updateBalance(@Param("accountId") Long accountId, @Param("currency") String currency, @Param("amount") BigDecimal amount);
}