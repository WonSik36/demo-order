package me.wonsik.order.demo.order.adapter.jpa

import javax.persistence.AttributeConverter
import javax.persistence.Converter


/**
 * @author 정원식 (wonsik.cheung)
 */
@Converter
class BooleanToYnConverter: AttributeConverter<Boolean, String> {

    override fun convertToDatabaseColumn(attribute: Boolean?): String =
        if (true == attribute) {
            YES
        } else {
            NO
        }

    override fun convertToEntityAttribute(dbData: String?): Boolean =
        YES == dbData

    companion object {
        const val YES: String = "Y"
        const val NO: String = "N"
    }
}