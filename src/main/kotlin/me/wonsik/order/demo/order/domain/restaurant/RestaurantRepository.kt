package me.wonsik.order.demo.order.domain.restaurant

import org.springframework.data.jpa.repository.JpaRepository


/**
 * @author 정원식 (wonsik.cheung)
 */
interface RestaurantRepository : JpaRepository<Restaurant, Long> {

}