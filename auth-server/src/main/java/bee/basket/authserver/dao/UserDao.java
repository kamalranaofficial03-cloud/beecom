package bee.basket.authserver.dao;


import bee.basket.authserver.entity.UserEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@ConditionalOnProperty(prefix = "app.datasource", name="provider", havingValue = "rd", matchIfMissing =false)
public interface UserDao extends JpaRepository<UserEntity,Long> {
    UserEntity findByUsername(String username);
    @Query("""
            SELECT CASE WHEN COUNT(u.username) > 0
            THEN TRUE ELSE FALSE END
            FROM UserEntity u
            WHERE u.username = :username
            """)
    boolean checkIfExist(@Param("username") String username);
}
