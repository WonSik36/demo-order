package me.wonsik.order.demo.order.domain.user

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
internal class UserRepositoryTest : BehaviorSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var repository: UserRepository

    init {
        given("사용자를 입력한다.") {
            val user = User(null, "alice", 17, Sex.MALE, "abc@example.com")
            repository.saveAndFlush(user)

            and("사용자를 업데이트 한다.") {
                user.email = "def@example.com"
                repository.saveAndFlush(user)

                `when`("사용자의 이메일과 생성날짜, 수정날짜를 검증한다.") {
                    val result = repository.findById(user.sequence!!).get()

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