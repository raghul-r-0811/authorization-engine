package org.raghul.auth_engine.security;

import org.raghul.auth_engine.entity.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Stream;

public class CustomUserDetails implements UserDetails {
    private UserEntity user;
    public CustomUserDetails(UserEntity user){
        this.user = user;
    }

    public UserEntity getUser() {
        return user;
    }

    public List<String> getRoleNames() {
       // System.out.println("----------temp buff--------------");
       // return "temp buff";
        return user.getUserRoles().stream()
                .map(UserRoleEntity::getRole)
                .map(RolesEntity::getRoleName)
                .toList();
    }




    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (UserRoleEntity userRole : user.getUserRoles()) {
            if (userRole.getRole() == null) {
                continue;
            }

            RolesEntity role = userRole.getRole();

            if (role.getRoleName() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName()));
            }

            if (role.getRolePermission() != null) {
                for (RolePermissionEntity rolePermission : role.getRolePermission()) {
                    if (rolePermission.getPermission() == null) {
                        continue;
                    }

                    PermissionEntity permission = rolePermission.getPermission();

                    if (permission.getPermissionName() != null) {
                        authorities.add(new SimpleGrantedAuthority(permission.getPermissionName()));
                    }
                }
            }
        }

        /*System.out.println("Authorities added for user " + user.getEmail() + ":");
        for (GrantedAuthority authority : authorities) {
            System.out.println(authority.getAuthority());
        }*/

        return authorities;
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
