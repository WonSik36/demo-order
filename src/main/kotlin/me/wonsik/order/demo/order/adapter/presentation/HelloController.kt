package me.wonsik.order.demo.order.adapter.presentation

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


/**
 * @author 정원식 (wonsik.cheung)
 */
@RestController
class HelloController {

    @GetMapping("hello")
    fun hello(): String = "hello world!";
}