package me.wonsik.order.demo.order

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class DemoOrderApplication

fun main(args: Array<String>) {
    runApplication<DemoOrderApplication>(*args)
}
