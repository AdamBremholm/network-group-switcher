package org.kepr.auth

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.web.util.matcher.AntPathRequestMatcher


@TestConfiguration
@Order(1)
class SecurityConfig : WebSecurityConfigurer<WebSecurity?> {

    override fun init(builder: WebSecurity?) {
        builder?.ignoring()?.requestMatchers(
                AntPathRequestMatcher("/**"))
    }

    override fun configure(builder: WebSecurity?) {
    }

}