package me.wonsik.order.demo.order.domain.common

import javax.persistence.Column
import javax.persistence.Embeddable


/**
 * @author 정원식 (wonsik.cheung)
 */
@Embeddable
data class Address (
    @Column val state: String,
    @Column val city: String,
    @Column val street: String,
    @Column val subStreet: String = "",
    @Column val zipCode: Int
)