package me.wonsik.order.demo.order.jpa.ch15

import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import me.wonsik.order.demo.order.domain.test.Frog
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
            createQuery("delete from FatFrog ff").executeUpdate()
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

        "영속성 컨텍스트와 프록시" - {
            "프록시 조회후, 원본엔티티 조회" {
                tx {
                    val spider = Spider("Alice", 2)
                    persist(spider)
                }

                tx {
                    val proxy = getReference(Spider::class.java, "Alice")
                    println("Proxy Class: ${proxy::class}")

                    val entity = find(Spider::class.java, "Alice")
                    println("Entity Class: ${entity::class}")

                    proxy shouldBeSameInstanceAs entity
                    proxy::class shouldBeSameInstanceAs entity::class
                }
            }

            "원본엔티티 조회후 프록시 조회" {
                tx {
                    val spider = Spider("Alice", 2)
                    persist(spider)
                }

                tx {
                    val entity = find(Spider::class.java, "Alice")
                    println("Entity Class: ${entity::class}")

                    val proxy = getReference(Spider::class.java, "Alice")
                    println("Proxy Class: ${proxy::class}")

                    proxy shouldBeSameInstanceAs entity
                    proxy::class shouldBeSameInstanceAs entity::class
                }
            }
        }

        "프록시 비교" - {
            "프록시 타입 비교" {
                tx {
                    val proxy = getReference(Spider::class.java, "Alice")

                    Spider::class shouldNotBe proxy::class
                    proxy.shouldBeInstanceOf<Spider>()
                }
            }

            "프록시 동등성 비교" {
                tx {
                    val spider = Spider("Alice", 2)
                    persist(spider)
                }

                tx {
                    val proxy = getReference(Spider::class.java, "Alice")
                    val spider = Spider("Alice", 2)

                    // equals
                    spider shouldBe proxy
                    proxy shouldBe spider

                    // hasCode
                    spider.hashCode() shouldBe proxy.hashCode()

                    // toString
                    spider.toString() shouldBe proxy.toString()
                }
            }
        }

        "N+1 문제" {
            var sequence: Long? = null
            tx {
                for (idx in 1..20) {
                    val frog = Frog()
                    persist(frog)
                    sequence = frog.sequence
                }
            }

            tx {
                val frogs = createQuery("select f from Frog f where f.sequence <= :sequence", Frog::class.java)
                    .setParameter("sequence", sequence)
                    .resultList

                /* Frog 조회 x 2회 */
                /*
                    select
                        frog0_.sequence as sequence1_2_
                    from
                        frog frog0_
                    where
                        frog0_.sequence<=?
                */

                /* WeakFrog 조회 */
                /*
                    select
                        weakfrogs0_.frog_sequence as frog_seq2_14_1_,
                        weakfrogs0_.sequence as sequence1_14_1_,
                        weakfrogs0_.sequence as sequence1_14_0_,
                        weakfrogs0_.frog_sequence as frog_seq2_14_0_
                    from
                        weak_frog weakfrogs0_
                    where
                        weakfrogs0_.frog_sequence in (
                            ?, ?, ?, ?, ?, ?, ?, ?, ?, ?
                        )
                */

                /* FatFrog 조회 */
                /*
                    select
                        fatfrogs0_.frog_sequence as frog_seq2_1_1_,
                        fatfrogs0_.sequence as sequence1_1_1_,
                        fatfrogs0_.sequence as sequence1_1_0_,
                        fatfrogs0_.frog_sequence as frog_seq2_1_0_
                    from
                        fat_frog fatfrogs0_
                    where
                        fatfrogs0_.frog_sequence in (
                            select
                                frog0_.sequence
                            from
                                frog frog0_
                            where
                                frog0_.sequence<=?
                        )
                */
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