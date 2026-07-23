package org.raghul.auth_engine.security;

import org.raghul.auth_engine.entity.UserEntity;
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

    public String getRoleName() {
        return user.getRole().getRoleName();
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
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
        return user.getTenantId();
    }

    public int getUserId(){
        return user.getuId();
    }
}
