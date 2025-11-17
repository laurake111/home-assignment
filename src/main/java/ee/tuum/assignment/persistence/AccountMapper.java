package ee.tuum.assignment.persistence;

import ee.tuum.assignment.model.Account;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AccountMapper {
	@Insert("INSERT INTO accounts (customer_id, country) VALUES (#{customerId}, #{country})")
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insert(Account account);

	@Select("SELECT id, customer_id, country, created_at, updated_at FROM accounts WHERE id = #{id}")
	Account findById(@Param("id") Long id);
}