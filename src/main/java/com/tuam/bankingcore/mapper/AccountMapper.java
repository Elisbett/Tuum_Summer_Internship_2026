package com.tuam.bankingcore.mapper;

import com.tuam.bankingcore.model.Account;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AccountMapper {

    @Insert("INSERT INTO account (customer_id, country) VALUES (#{customerId}, #{country})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(Account account);

    @Select("SELECT id, customer_id as customerId, country, created_at as createdAt FROM account WHERE id = #{id}")
    /*@Results(id = "accountResultMap", value = {
        @Result(property = "id", column = "id"),
        @Result(property = "customerId", column = "customer_id"),
        @Result(property = "country", column = "country"),
        @Result(property = "createdAt", column = "created_at")
    })*/
    Account findById(@Param("id") Long id);
}