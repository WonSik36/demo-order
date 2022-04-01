package me.wonsik.order.demo.order.domain.user

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import me.wonsik.order.demo.order.domain.common.Address
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDate

@DataJpaTest
internal class UserRepositoryTest : BehaviorSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var repository: UserRepository

    private val birthDay: LocalDate = LocalDate.now().minusYears(21)
    private val address = Address("state", "city", "street", "subStreet", 12345)

    init {
        given("사용자를 입력한다.") {
            val user = User(null, "alice", birthDay, Sex.MALE, "abc@example.com", address)
            repository.saveAndFlush(user)

            and("사용자를 업데이트 한다.") {
                user.email = "def@example.com"
                repository.saveAndFlush(user)

                `when`("사용자의 이메일과 생성날짜, 수정날짜를 검증한다.") {
                    val sequence = user.sequence!!
                    println("Generated User Sequence: $sequence")
                    val result = repository.findById(sequence).get()

                    then("21살이어야한다.") {
                        result.age shouldBe 21
                    }

                    then("이메일이 변경 되어있어야한다.") {
                        result.email shouldBe "def@example.com"
                    }

                    then("생성날짜와 수정날짜가 달라야한다.") {
                        println("created date: ${result.createdDateTime}, updated date: ${result.updatedDateTime}")
                        result.createdDateTime shouldNotBe result.updatedDateTime
                    }
                }
            }
        }
    }
}