package org.kepr.gateway.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.web.client.RestTemplate


@Configuration
@EnableConfigurationProperties(JwtProperties::class)
class ApplicationConfig(val jwtProperties: JwtProperties) {

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder(jwtProperties.strength)

    @LoadBalanced
    @Bean
    fun restTemplate(): RestTemplate? {
        return RestTemplate()
    }
}