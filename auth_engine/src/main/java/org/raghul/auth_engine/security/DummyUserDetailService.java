package org.raghul.auth_engine.security;

import org.raghul.auth_engine.entity.UserEnity;
import org.raghul.auth_engine.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service("dummyUserDetailsService")
public class DummyUserDetailService implements UserDetailsService {

    protected UserRepo userRepo;

    @Autowired
    public DummyUserDetailService(UserRepo userRepo){
        this.userRepo = userRepo;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=====================loadUserByName in CustomerUserDetailService ==============================");
        UserEnity user = userRepo.findByEmail(username);
        if(user == null ){
            throw new UsernameNotFoundException("Email id not found");
        }
        UserDetails userDetails = new CustomUserDetails(user);
        return userDetails;
    }
}
