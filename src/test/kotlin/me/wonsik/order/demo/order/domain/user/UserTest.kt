package me.wonsik.order.demo.order.domain.user

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import me.wonsik.order.demo.order.domain.common.Address
import java.time.LocalDate

internal class UserTest : BehaviorSpec() {
    private val address = Address("state", "city", "street", "subStreet", 12345)

    init {
        given("사용자의 나이가 17세이다.") {
            val birthDay: LocalDate = LocalDate.now().minusYears(17)
            val user = User(1, "alice", birthDay, Sex.MALE, "abc@example.com", address)

            `when`("성인 여부를 검증한다.") {
                val result = user.isAdult()

                then("성인이 아니여야한다.") {
                    result shouldBe false
                }
            }

            `when`("성별을 확인한다.") {
                val sex = user.sex

                then("남성이어야한다.") {
                    sex shouldBe Sex.MALE
                }
            }
        }

        given("사용자의 나이가 18세이다.") {
            val birthDay: LocalDate = LocalDate.now().minusYears(18)
            val user = User(1, "alice", birthDay, Sex.MALE, "abc@example.com", address)

            `when`("성인 여부를 검증한다.") {
                val result = user.isAdult()

                then("성인이어야한다.") {
                    result shouldBe true
                }
            }
        }
    }
}