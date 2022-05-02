package me.wonsik.order.demo.order.coroutines.channel

import io.kotest.core.spec.style.FreeSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.job


/**
 * @author 정원식 (wonsik.cheung)
 *
 * 참조
 * @see <a href="https://dalinaum.github.io/coroutines-example/18">채널 파이프라인</a>
 */
@ExperimentalCoroutinesApi
class ChannelPipelineTest : FreeSpec({

    "Pipeline" {
        val numbers = produceNumbers()
        val stringNumbers = produceStringNumbers(numbers)

        repeat(5) {
            println(stringNumbers.receive())
        }

        println("Success")
        coroutineContext.cancelChildren()
    }

    "Pipeline - Odd numbers" {
        val numbers = produceNumbers()
        val oddNumbers = filterOdd(numbers)

        repeat(10) {
            println(oddNumbers.receive())
        }

        println("Success")
        coroutineContext.cancelChildren()
    }

    "Pipeline - Prime numbers" {
        var numbers = numbersForm(2)

        repeat(10) {
            val prime = numbers.receive()
            println(prime)
            numbers = filter(numbers, prime)
        }

        println("Success")
        coroutineContext.cancelChildren()
    }
})

@ExperimentalCoroutinesApi
fun CoroutineScope.produceNumbers() = produce<Int> {
    var x = 1
    while (true) {
        send(x++)
    }
}

@ExperimentalCoroutinesApi
fun CoroutineScope.produceStringNumbers(numbers: ReceiveChannel<Int>): ReceiveChannel<String> = produce {
    for (i in numbers) {
        send("${i}!")
    }
}

@ExperimentalCoroutinesApi
fun CoroutineScope.filterOdd(numbers: ReceiveChannel<Int>): ReceiveChannel<String> = produce {
    for (i in numbers) {
        if (i % 2 == 1) {
            send("${i}!")
        }
    }
}

@ExperimentalCoroutinesApi
fun CoroutineScope.numbersForm(start: Int) = produce<Int> {
    var x = start
    while (true) {
        println("${coroutineContext.job}: send $x")
        send(x++)
    }
}

@ExperimentalCoroutinesApi
fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int): ReceiveChannel<Int> = produce {
    for (i in numbers) {
        println("${coroutineContext.job}: $i % $prime = ${i % prime}")
        if (i % prime != 0) {
            send(i)
        }
    }
}