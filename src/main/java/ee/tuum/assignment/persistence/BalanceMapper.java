package ee.tuum.assignment.persistence;

import ee.tuum.assignment.model.Balance;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface BalanceMapper {

	@Insert("INSERT INTO balances (account_id, currency, available_amount) " +
			"VALUES (#{accountId}, #{currency}, #{availableAmount})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insertBalance(Balance balance);

	@Select("SELECT id, account_id, currency, available_amount, created_at, updated_at " +
			"FROM balances WHERE account_id = #{accountId}")
	List<Balance> findByAccountId(@Param("accountId") Long accountId);

	@Select("SELECT id, account_id, currency, available_amount, created_at, updated_at " +
			"FROM balances WHERE account_id = #{accountId} AND currency = #{currency} FOR UPDATE")
	Balance findByAccountIdAndCurrency(@Param("accountId") Long accountId,
									   @Param("currency") String currency);


	@Update("UPDATE balances SET available_amount = #{newAmount}, updated_at = now() " +
			"WHERE account_id = #{accountId} AND currency = #{currency}")
	void updateBalance(@Param("accountId") Long accountId,
					   @Param("currency") String currency,
					   @Param("newAmount") BigDecimal newAmount);
}
