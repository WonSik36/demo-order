package me.wonsik.order.demo.order.coroutines.channel

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/19">팬 아웃, 팬 인</a>
 */
class FanOutFanInTest : FreeSpec({

    "Fan out" {
        val producer = produceNumbers()
        val list = MutableList(5) { 0 }

        repeat(5) {
            launch {
                // Producer < Consumer
                producer.consumeEach { value ->
                    println("${it}가 ${value}을 받았습니다.")
                    list[it] = list[it] + 1
                }
            }
        }

        delay(20L)
        producer.cancel()   // ProducerCoroutine -> ChannelCoroutine 에서 cancel 되면 안의 코루틴도 cancel 함
        println(list)   // not equal
    }

    "Fan in" {
        val channel = Channel<Int>()

        // Producer > Consumer
        produceNumbers(channel, 1, 100)
        produceNumbers(channel, 2, 100)

        retrieveNumbers(channel)

        delay(1000L)
        coroutineContext.cancelChildren()
    }

    "Balancing" {
        val channel = Channel<Int>()
        val list = MutableList(5) { 0 }

        for(it in 0 until  5) {
            retrieveAndSend(channel, list, it)
        }

        channel.send(1)
        delay(20L)
        coroutineContext.cancelChildren()
        println(list)   // equal
    }
})

fun CoroutineScope.produceNumbers(channel: SendChannel<Int>, from: Int, interval: Long) = launch {
    var x = from
    while(true) {
        channel.send(x)
        x += 2
        delay(interval)
    }
}

fun CoroutineScope.retrieveNumbers(channel: ReceiveChannel<Int>) = launch {
    channel.consumeEach {
        println("${it}을 받았습니다.")
    }
}

fun CoroutineScope.retrieveAndSend(channel: Channel<Int>, list: MutableList<Int>, index: Int) = launch {
    for (value in channel) {
        println("$index 가 ${value} 를 받았습니다.")
        list[index] = list[index] + 1
        channel.send(value + 1)
    }
}