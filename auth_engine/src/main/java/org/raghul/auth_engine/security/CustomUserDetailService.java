package org.raghul.auth_engine.security;

import org.raghul.auth_engine.entity.UserEnity2;
import org.raghul.auth_engine.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
This is the class that asks the repo layer to check for whether there is a user or not
with this email. If yes it will create a UserDetails  object which will reperesent the user entity
for Spring Security.(UserDetail is used by Spring Security for ---- xxx --- fill it once understood completely)
 */



@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEnity2 user = userRepo.findByEmail(username);
        if(user == null ){
            throw new UsernameNotFoundException("Email id not found");
        }
        UserDetails userDetails = new CustomUserDetails(user);
        return userDetails;
    }
}
