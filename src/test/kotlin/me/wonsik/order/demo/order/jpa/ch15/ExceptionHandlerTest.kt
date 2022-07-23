package me.wonsik.order.demo.order.jpa.ch15

import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import me.wonsik.order.demo.order.domain.test.Spider
import org.springframework.boot.test.context.SpringBootTest
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceUnit


/**
 * @author 정원식 (wonsik.cheung)
 */
@SpringBootTest
class ExceptionHandlerTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @PersistenceUnit
    private lateinit var emf: EntityManagerFactory

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        tx {
            createQuery("delete from Spider s").executeUpdate()
        }
    }

    init {
        "JPA 표준 예외" - {
            "트랜잭션 롤백을 표시하는 예외 - 강제 커밋 불가능" {
                tx {
                    val spider = Spider("Alice", 1)
                    persist(spider)
                }

                val em = emf.createEntityManager()
                val tx = em.transaction
                try {
                    tx.begin()
                    val spider = Spider("Bob", 2)
                    em.persist(spider)
                    em.flush()

                    val spider2 = Spider("Alice", 2)
                    em.persist(spider2)
                    em.flush()  // Exception Occurred: PersistenceException
                    tx.commit()
                } catch (e: RuntimeException) {
                    tx.rollbackOnly shouldBe true
                    e.printStackTrace()
                    tx.commit() // 강제로 커밋하나 롤백 처리됨 JdbcResourceLocalTransactionCoordinatorImpl#commit
                } finally {
                    em.close()
                }

                tx {
                    val count = createQuery("select count(s) from Spider s")
                        .singleResult

                    count shouldBe 1
                }
            }

            "트랜잭션 롤백을 표시하지 않는 예외 - 강제 커밋 가능" {
                val em = emf.createEntityManager()
                val tx = em.transaction

                try {
                    tx.begin()
                    val spider = Spider("Alice", 1)
                    em.persist(spider)

                    val spider2 = em.createQuery("select s from Spider s where s.name = :name")
                        .setParameter("name", "Bob")
                        .singleResult   // Exception Occurred: NoResultException
                    tx.commit()
                } catch (e: RuntimeException) {
                    tx.rollbackOnly shouldBe false
                    e.printStackTrace()
                    tx.commit() // 강제로 커밋
                } finally {
                    em.close()
                }

                tx {
                    val count = createQuery("select count(s) from Spider s")
                        .singleResult

                    count shouldBe 1
                }
            }

            "트랜잭션 롤백을 표시하는 예외임에도 불구하고 강제 커밋 가능" {
                val em = emf.createEntityManager()
                val tx = em.transaction
                try {
                    tx.begin()
                    val spider = Spider("Bob", 2)
                    em.persist(spider)
                    em.flush()

                    val spider2 = em.getReference(Spider::class.java, "Chris")
                    println(spider2.age)   // Exception Occurred: EntityNotFoundException
                    tx.commit()
                } catch (e: RuntimeException) {
                    tx.rollbackOnly shouldBe false
                    e.printStackTrace()
                    tx.commit() // 강제로 커밋
                } finally {
                    em.close()
                }

                tx {
                    val count = createQuery("select count(s) from Spider s")
                        .singleResult

                    count shouldBe 1
                }
            }
        }
    }

    private inline fun tx(logic: EntityManager.() -> Unit) {
        val em = emf.createEntityManager()
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
}