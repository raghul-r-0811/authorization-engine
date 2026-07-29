package org.raghul.auth_engine.security;

import org.raghul.auth_engine.entity.RolesEntity;
import org.raghul.auth_engine.entity.UserEntity;
import org.raghul.auth_engine.entity.UserRoleEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private UserEntity user;
    public CustomUserDetails(UserEntity user){
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }

    public List<String> getRoleNames() {
        System.out.println("----------temp buff--------------");
       // return "temp buff";
        return user.getUserRoles().stream()
                .map(UserRoleEntity::getRole)
                .map(RolesEntity::getRoleName)
                .toList();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getUserRoles().stream()
                .map(UserRoleEntity::getRole)
                .filter(role -> role != null && role.getRoleName() != null)
                .map(RolesEntity::getRoleName)
                .distinct()
                .map(roleName -> new SimpleGrantedAuthority("ROLE_" + roleName))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {

        // as of now returning email later change it to whatever is necessary.16/04/2026
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getEmail(){
        return user.getEmail();
    }

    public Integer getTenantId(){
        return user.getTenant() != null ? user.getTenant().getTenantId() : null;
    }

    public int getUserId(){
        return user.getuId();
    }
}
