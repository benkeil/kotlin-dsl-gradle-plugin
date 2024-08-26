package pub.keil.kotlin.dsl.model

import com.squareup.kotlinpoet.TypeName

data class DslClass(
    val packageName: String,
    val name: String,
    val description: String?,
    val properties: List<DslProperty> = emptyList(),
) {
  val canonicalName: String
    get() = "$packageName.$name"
}

data class DslProperty(
    val name: String,
    val type: TypeName,
    val description: String?,
    val required: Boolean,
    val isObject: Boolean,
)
