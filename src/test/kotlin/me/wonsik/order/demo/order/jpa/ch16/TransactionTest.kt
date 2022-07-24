package me.wonsik.order.demo.order.jpa.ch16

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.test.TestCase
import io.kotest.core.test.TestResult
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import me.wonsik.order.demo.order.adapter.querydsl.QueryDslConfig
import me.wonsik.order.demo.order.domain.test.TestLike
import me.wonsik.order.demo.order.domain.test.TestPost
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import javax.persistence.*


/**
 * @author 정원식 (wonsik.cheung)
 */
@DataJpaTest
@Import(QueryDslConfig::class)
class TransactionTest : FreeSpec() {
    override fun extensions() = listOf(SpringExtension)

    @PersistenceUnit
    private lateinit var emf: EntityManagerFactory

    override suspend fun afterEach(testCase: TestCase, result: TestResult) {
        tx {
            createQuery("delete from TestPost tp").executeUpdate()
            createQuery("delete from TestLike tl").executeUpdate()
        }
    }

    init {
        "@Version" {
            var sequence: Long? = null

            tx {
                val like = TestLike()
                persist(like)
                sequence = like.sequence
            }


            val throwable = shouldThrow<RollbackException> {
                val em = emf.createEntityManager()
                val tx = em.transaction
                tx.begin()

                // Transaction 1
                val like = em.find(TestLike::class.java, sequence)

                // Transaction 2
                tx {
                    val like = find(TestLike::class.java, sequence)
                    like.plusCount()
                }

                // Transaction 1
                like.plusCount()
                tx.commit() // OptimisticLockException
            }

            throwable.cause.shouldBeInstanceOf<OptimisticLockException>()
        }

        "LockModeType" - {
            "NONE" {
                var sequence: Long? = null

                tx {
                    val like = TestLike()
                    persist(like)
                    sequence = like.sequence
                }


                val throwable = shouldThrow<RollbackException> {
                    val em = emf.createEntityManager()
                    val tx = em.transaction
                    tx.begin()

                    // Transaction 1
                    val like = em.find(TestLike::class.java, sequence, LockModeType.NONE)

                    // Transaction 2
                    tx {
                        val like = find(TestLike::class.java, sequence, LockModeType.NONE)
                        like.plusCount()
                    }

                    // Transaction 1
                    like.plusCount()
                    tx.commit() // OptimisticLockException
                }

                throwable.cause.shouldBeInstanceOf<OptimisticLockException>()
            }

            "OPTIMISTIC" {
                var sequence: Long? = null

                tx {
                    val like = TestLike()
                    persist(like)
                    sequence = like.sequence
                }


                val throwable = shouldThrow<RollbackException> {
                    val em = emf.createEntityManager()
                    val tx = em.transaction
                    tx.begin()

                    // Transaction 1
                    val like = em.find(TestLike::class.java, sequence, LockModeType.OPTIMISTIC)

                    // Transaction 2
                    tx {
                        val like = find(TestLike::class.java, sequence, LockModeType.OPTIMISTIC)
                        like.plusCount()
                    }

                    // Transaction 1
                    // like.plusCount()

                    // 커밋시, 버전 체크
                    tx.commit() // OptimisticLockException
                }

                throwable.cause.shouldBeInstanceOf<OptimisticLockException>()
            }

            "OPTIMISTIC_FORCE_INCREMENT" {
                var sequence: Long? = null

                tx {
                    val post = TestPost(name = "Hello")
                    val like = TestLike()
                    post.like = like
                    persist(like)
                    persist(post)
                    sequence = post.sequence
                }

                // Without LockMode
                println("*****************************************************************************")
                tx {
                    val post = find(TestPost::class.java, sequence)
                    post.like?.plusCount()
                }

                tx {
                    val post = find(TestPost::class.java, sequence)
                    post.version shouldBe 0
                }

                // LockModeType.OPTIMISTIC_FORCE_INCREMENT
                // 조회만 해도 버전업
                println("=============================================================================")
                tx {
                    val post = find(TestPost::class.java, sequence, LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                    post.like?.plusCount()
                }

                tx {
                    val post = find(TestPost::class.java, sequence)
                    post.version shouldBe 1
                }

                // LockModeType.OPTIMISTIC_FORCE_INCREMENT
                // 조회+수정시 버전업 +2
                println("||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||")
                tx {
                    val post = find(TestPost::class.java, sequence, LockModeType.OPTIMISTIC_FORCE_INCREMENT)
                    val like = TestLike()
                    persist(like)
                    post.like = like
                }

                tx {
                    val post = find(TestPost::class.java, sequence)
                    post.version shouldBe 3
                }
            }

            "PESSIMISTIC_READ" {
                var sequence: Long? = null

                tx {
                    val like = TestLike()
                    persist(like)
                    sequence = like.sequence
                }

                val hints = mapOf<String, Any>("javax.persistence.lock.timeout" to 500, "javax.persistence.query.timeout" to 500)

                tx {
                    // Transaction 1
                    val like = find(TestLike::class.java, sequence, LockModeType.PESSIMISTIC_READ)  // select for update

                    // Transaction 2
                    val ex = txWithException {
                        val like = find(TestLike::class.java, sequence, hints)
                        like.plusCount()
                    }
                    ex.shouldBeInstanceOf<RollbackException>()
                    ex.cause.shouldBeInstanceOf<PessimisticLockException>()

                    // Transaction 1
                    like.plusCount()
                } // update count and version

                tx {
                    val like = find(TestLike::class.java, sequence)
                    like.version shouldBe 1
                }
            }

            "PESSIMISTIC_WRITE" {
                var sequence: Long? = null

                tx {
                    val like = TestLike()
                    persist(like)
                    sequence = like.sequence
                }

                val hints = mapOf<String, Any>("javax.persistence.lock.timeout" to 500, "javax.persistence.query.timeout" to 500)

                tx {
                    // Transaction 1
                    val like = find(TestLike::class.java, sequence, LockModeType.PESSIMISTIC_WRITE) // select for update

                    // Transaction 2
                    val ex = txWithException {
                        val like = find(TestLike::class.java, sequence, hints)
                        like.plusCount()
                    }
                    ex.shouldBeInstanceOf<RollbackException>()
                    ex.cause.shouldBeInstanceOf<PessimisticLockException>()

                    // Transaction 1
                    like.plusCount()
                } // update count and version

                tx {
                    val like = find(TestLike::class.java, sequence)
                    like.version shouldBe 1
                }
            }

            "PESSIMISTIC_FORCE_INCREMENT" {
                var sequence: Long? = null

                tx {
                    val like = TestLike()
                    persist(like)
                    sequence = like.sequence
                }

                val hints = mapOf<String, Any>("javax.persistence.lock.timeout" to 500, "javax.persistence.query.timeout" to 500)

                tx {
                    // Transaction 1
                    val like = find(TestLike::class.java, sequence, LockModeType.PESSIMISTIC_FORCE_INCREMENT) // select for update
                    // update version

                    // Transaction 2
                    val ex = txWithException {
                        val like = find(TestLike::class.java, sequence, hints)
                        like.plusCount()
                    }
                    ex.shouldBeInstanceOf<RollbackException>()
                    ex.cause.shouldBeInstanceOf<PessimisticLockException>()

                    // Transaction 1
                    like.plusCount()
                } // update count and version

                tx {
                    val like = find(TestLike::class.java, sequence)
                    like.version shouldBe 2
                }
            }
        }
    }

    private inline fun tx(logic: EntityManager.() -> Unit) {
        val em = emf.createEntityManager()

        tx(em, logic)
    }

    private inline fun tx(em: EntityManager, body: EntityManager.() -> Unit) {
        val tx = em.transaction

        try {
            tx.begin()
            body(em)
            tx.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            tx.rollback()
            throw e
        } finally {
            em.close()
        }
    }

    private inline fun txWithException(body: EntityManager.() -> Unit): Exception? {
        val em = emf.createEntityManager()
        val tx = em.transaction

        try {
            tx.begin()
            body(em)
            tx.commit()
        } catch (e: Exception) {
            e.printStackTrace()
            tx.rollback()
            return e
        } finally {
            em.close()
        }

        return null
    }
}