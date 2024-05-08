package com.web.persistence;

import com.web.repo.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends CrudRepository<User,Long> {
    @Query(value = "SELECT * FROM dwbbmember WHERE id=:id AND pw=:pw", nativeQuery = true)
    User loginCheckByIdPw(@Param("id") String id, @Param("pw") String pw);

    @Query(value = "SELECT * FROM dwbbmember WHERE email=:email", nativeQuery = true)
    User findUserByEmail(@Param("email") String email);
}
