package org.kepr.gateway.config

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class MyUserDetailsService : UserDetailsService {
    override fun loadUserByUsername(username: String?): UserDetails {
        TODO("Not yet implemented")
    }

}