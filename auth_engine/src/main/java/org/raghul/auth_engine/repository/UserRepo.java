package org.raghul.auth_engine.repository;


import org.raghul.auth_engine.entity.TenantEntity;
import org.raghul.auth_engine.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<UserEntity,Integer> {

    UserEntity findByEmail(String username);
    boolean existsByEmail(String email);
    boolean existsByTenantAndEmail(TenantEntity tenant, String email);


    @Query("""
    SELECT DISTINCT u
    FROM UserEntity u
    LEFT JOIN FETCH u.userRoles ur
    LEFT JOIN FETCH ur.role r
    LEFT JOIN FETCH r.rolePermission rp
    LEFT JOIN FETCH rp.permission p
    LEFT JOIN FETCH u.tenant t
    WHERE u.email = :email
""")
    Optional<UserEntity> findByEmailWithRolesAndPermissions(@Param("email") String email);
}
