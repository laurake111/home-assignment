package ee.tuum.assignment.persistence;

import ee.tuum.assignment.model.Transaction;
import org.apache.ibatis.annotations.*;

@Mapper
public interface TransactionMapper {

    @Insert("INSERT INTO transactions (account_id, amount, currency, direction, description) " +
            "VALUES (#{accountId}, #{amount}, #{currency}, #{direction}, #{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    void insert(Transaction transaction);

    @Select("SELECT id, account_id, currency, amount, direction, " +
            "description, created_at, updated_at FROM transactions WHERE id = #{id}")
    Transaction findById(@Param("id") Long id);
}
