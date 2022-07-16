package me.wonsik.order.demo.order.adapter.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import javax.persistence.EntityManagerFactory
import javax.persistence.PersistenceUnit


/**
 * @author 정원식 (wonsik.cheung)
 */
@Repository
class EntityManagerFactoryHolder {
    // 고정된 EntityManagerFactory 를 사용한다.
    @PersistenceUnit
    lateinit var persistenceUnitEntityManagerFactory: EntityManagerFactory
    @Autowired
    lateinit var autowiredEntityManagerFactory: EntityManagerFactory
}