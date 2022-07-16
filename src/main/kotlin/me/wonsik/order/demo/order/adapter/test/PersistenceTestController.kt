package me.wonsik.order.demo.order.adapter.test

import me.wonsik.order.demo.order.adapter.presentation.HelloController
import org.hibernate.internal.SessionFactoryImpl
import org.hibernate.internal.SessionImpl
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


/**
 * @author 정원식 (wonsik.cheung)
 */
@RequestMapping("test")
@RestController
class PersistenceTestController(
    val emfHolder: EntityManagerFactoryHolder,
    val emHolder: EntityManagerHolder,
) {
    private val logger = LoggerFactory.getLogger(HelloController::class.java)

    @GetMapping("autowiredEntityManagerFactory")
    fun autowiredEntityManagerFactory(): String {
        val target = emfHolder.autowiredEntityManagerFactory.unwrap(SessionFactoryImpl::class.java)
        logger.info(target.toString())
        return target.toString()
    }

    @GetMapping("persistenceUnitEntityManagerFactory")
    fun persistenceUnitEntityManagerFactory(): String {
        val target = emfHolder.persistenceUnitEntityManagerFactory.unwrap(SessionFactoryImpl::class.java)
        logger.info(target.toString())
        return target.toString()
    }

    @GetMapping("autowiredEntityManager")
    fun autowiredEntityManager(): String {
        val target = emHolder.autowiredEntityManager.unwrap(SessionImpl::class.java)
        logger.info(target.toString())
        return target.toString()
    }

    @GetMapping("persistenceContextEntityManager")
    fun persistenceContextEntityManager(): String {
        val target = emHolder.persistenceContextEntityManager.unwrap(SessionImpl::class.java)
        logger.info(target.toString())
        return target.toString()
    }
}