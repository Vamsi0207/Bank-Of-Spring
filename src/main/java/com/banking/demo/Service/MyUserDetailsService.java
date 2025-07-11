package com.banking.demo.Service;

import com.banking.demo.Models.UserPrincipal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.banking.demo.Models.Account;
import com.banking.demo.Repo.AccountRepo;

@Service
public class MyUserDetailsService implements UserDetailsService {
    
    @Autowired
    AccountRepo accrepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        Account a=accrepo.findByEmail(email);
        if(a==null){
            System.out.println("Account does not exist");
            throw new UsernameNotFoundException("Account not found");
        }

        return new UserPrincipal(a);
    }

    
}
