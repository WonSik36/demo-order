package me.wonsik.order.demo.order.coroutines.channel

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.job
import kotlinx.coroutines.launch


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/17">채널 기초</a>
 */
@ExperimentalCoroutinesApi
class ChannelBasicTest : FreeSpec({
    "Basic" - {
        "send - receive" {
            val channel = Channel<Int>()
            launch {
                for (x in 1..10) {
                    println("${currentCoroutineContext().job} send: $x")
                    channel.send(x)     // suspension point
                }
            }

            println("시작")
            repeat(10) {
                println("${currentCoroutineContext().job} receive: ${channel.receive()}")  // suspension point
            }
            println("완료")
        }

        "trySend - tryReceive" {
            val channel = Channel<Int>(capacity = 5)

            println("시작")
            launch {
                for (x in 1..10) {
                    println("${currentCoroutineContext().job} trySend: $x")
                    channel.trySend(x)     // not suspension point
                }
            }.join()

            repeat(10) {
                println("${currentCoroutineContext().job} tryReceive: ${channel.tryReceive().getOrNull()}")  // not suspension point
            }
            println("완료")
        }
    }

    "close" {
        val channel = Channel<Int>()

        launch {
            for (x in 1..10) {
                channel.send(x)     // suspension point
            }
            channel.close()
        }

        for (value in channel) {
            println(value)
        }
        println("완료")
    }

    "ProducerCoroutine" {
        val channel = produce<Int> {      // ProducerCoroutine -> ProducerScope = CoroutineScope + SendChannel
            for(x in 1..10) {       // ProducerCoroutine -> ChannelCoroutine (AbstractCoroutine, Channel)
                channel.send(x)
            }
            channel.close()
        }

        channel.consumeEach {
            println(it)
        }
        println("완료")
    }
})