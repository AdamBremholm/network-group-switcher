package org.kepr.hostapi

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class HostApiApplication

fun main(args: Array<String>) {
    runApplication<HostApiApplication>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}
