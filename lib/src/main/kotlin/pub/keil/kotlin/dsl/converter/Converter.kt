package pub.keil.kotlin.dsl.converter

import com.squareup.kotlinpoet.FileSpec
import pub.keil.kotlin.dsl.model.DslClass

interface Converter {
    fun convert(dslClass: DslClass): FileSpec
}
