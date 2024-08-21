package pub.keil.kotlin.dsl.converter

import com.squareup.kotlinpoet.PropertySpec
import pub.keil.kotlin.dsl.model.DslProperty

fun escapeDoc(doc: String): String = doc.replace("/", "{@literal /}").replace("%", "%%")

fun toPropertySpec(property: DslProperty): PropertySpec {
  val type = property.type.copy(nullable = true)
  val builder = PropertySpec.builder(property.name, type).initializer("null").mutable()
  property.description?.let { description -> builder.addKdoc(escapeDoc(description)) }
  return builder.build()
}
