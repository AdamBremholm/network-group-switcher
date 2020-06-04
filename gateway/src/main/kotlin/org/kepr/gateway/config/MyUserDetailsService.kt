package org.kepr.gateway.config

import org.kepr.gateway.data.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.util.ArrayList

@Service
class MyUserDetailsService(@Autowired
                           val restTemplate: RestTemplate) : UserDetailsService {

    override fun loadUserByUsername(username: String?): UserDetails {
        val foundUser = restTemplate.getForObject("http://user-api-service/api/users/loadforauth/".plus(username), User::class.java)
                ?: throw UsernameNotFoundException("username: $username not found")
        val authorities = ArrayList<GrantedAuthority>()
        foundUser.roles.forEach { authorities.add(SimpleGrantedAuthority(it)) }

        return org.springframework.security.core.userdetails.User(foundUser.userName, foundUser.password, authorities)
    }

}