package me.wonsik.order.demo.order.jpa.ch3

import io.kotest.core.spec.style.FreeSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import me.wonsik.order.demo.order.domain.common.Address
import me.wonsik.order.demo.order.domain.user.Sex
import me.wonsik.order.demo.order.domain.user.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.lang.IllegalStateException
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
internal class PersistenceContextTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @Autowired
    private lateinit var emf: EntityManagerFactory  // thread-safe

    private val address = Address("state", "city", "street", "subStreet", 12345)
    private val address2 = Address("newState", "newCity", "newStreet", "newSubStreet", 67890)

    init {
        "엔티티 조회" - {
            "1차 캐시에서 조회" {
                tx {
                    val user = makeUser()

                    // 등록
                    persist(user)

                    // 1차 캐시에서 조회
                    val findUser = find(User::class.java, user.sequence)
                    findUser shouldBeSameInstanceAs user

                    log("END")
                }
            }

            "DB 에서 조회" {
                tx {
                    val user = makeUser()

                    // 등록
                    persist(user)
                    flush()
                    detach(user)

                    log("Find in DB")

                    // DB 에서 조회
                    val findUser = find(User::class.java, user.sequence)
                    findUser shouldBe user
                    findUser shouldNotBeSameInstanceAs user
                }
            }
        }

        "엔티티 등록" {
            tx {
                val user1 = makeUser()
                val user2 = makeUser()

                // 등록
                persist(user1)
                persist(user2)

                log("END")
            }

            // 트랜잭션 커밋 전에 insert 쿼리 실행
        }

        "엔티티 수정" {
            tx {
                val user = makeUser()

                // 등록
                persist(user)

                // 수정
                val newMail = "newEmail@example.com"
                user.email = newMail

                log("END")
            }

            // 트랜잭션 커밋 전에 insert & update 쿼리 실행
        }

        "엔티티 삭제" {
            tx {
                val user = makeUser()

                // 등록
                persist(user)

                // 삭제
                remove(user)

                log("END")
            }

            // 트랜잭션 커밋 전에 insert & delete 쿼리 실행
        }

        "준영속" - {
            "detach" {
                tx {
                    val user1 = makeUser()

                    // 등록
                    persist(user1)

                    // 준영속 상태
                    // 1차 캐시, 쓰기 지연 SQL 저장소에서 제거
                    detach(user1)
                }

                // 아무런 일도 일어나지 않음
            }

            "clear" {
                tx {
                    val user1 = makeUser()
                    val user2 = makeUser()

                    // 등록
                    persist(user1)
                    persist(user2)

                    // 준영속 상태
                    // 1차 캐시, 쓰기 지연 SQL 저장소에서 제거
                    clear()
                }

                // 아무런 일도 일어나지 않음
            }

            "close" {
                var user: User? = null
                tx {
                    user = makeUser()

                    // 등록
                    persist(user)
                }

                // 준영속 상태에서 업데이트
                // update 쿼리가 실행되지 않음
                user?.email = "newEmail@example.com"

                tx {
                    val findUser = find(User::class.java, user?.sequence)   // 식별자 값을 가지고 있음

                    findUser.email shouldNotBe "newEmail@example.com"
                    findUser.email shouldBe ""
                }
            }

            "merge - 준영속" {
                var user: User? = null
                tx {
                    user = makeUser()

                    // 등록
                    persist(user)
                }

                // 준영속 상태이므로 업데이트 안됨
                user!!.address = address2

                tx {
                    // 1. 1차 캐시 조회
                    // 2. DB 조회
                    // 3. 병합하기 (user 의 모든 값으로 값 채우기)
                    val mergedMember = merge(user) ?: throw IllegalStateException()
                    log("SELECT 실행 완료")

                    // 수정
                    mergedMember.email = "newEmail@example.com"

                    contains(mergedMember) shouldBe true
                    contains(user) shouldBe false

                    mergedMember.address.state shouldBe "newState"

                    log("UPDATE 실행")
                }

                tx {
                    val findUser = find(User::class.java, user?.sequence)   // 식별자 값을 가지고 있음

                    findUser.email shouldBe "newEmail@example.com"
                }
            }

            "merge - 비영속" {
                var user: User? = null
                tx {
                    user = makeUser()

                    // 등록
                    val newUser = merge(user)
                    contains(newUser) shouldBe true
                    contains(user) shouldBe false
                }
            }
        }
    }

    private inline fun tx(logic: EntityManager.() -> Unit) {
        val em = emf.createEntityManager()  // thread-unsafe
        val tx = em.transaction

        try {
            tx.begin()
            logic(em)
            tx.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            tx.rollback()
            throw e
        } finally {
            em.close()
        }
    }

    private fun makeUser() = User(null, "alice", birthDay = null, Sex.FEMALE, "", address)

    private fun log(message: String) {
        println("************************* $message *************************")
    }
}