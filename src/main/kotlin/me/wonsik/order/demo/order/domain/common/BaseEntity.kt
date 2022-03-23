package me.wonsik.order.demo.order.domain.common

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass


/**
 * @author 정원식 (wonsik.cheung)
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {

    @CreatedDate
    lateinit var createdDateTime: LocalDateTime

    @LastModifiedDate
    lateinit var updatedDateTime: LocalDateTime
}