package me.wonsik.order.demo.order.adapter.test

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext


/**
 * @author 정원식 (wonsik.cheung)
 */
@Repository
class EntityManagerHolder {
    // 매요청마다 새로 EntityManager 가 설정된다.
    @PersistenceContext
    lateinit var persistenceContextEntityManager: EntityManager
    @Autowired
    lateinit var autowiredEntityManager: EntityManager
}