package pub.keil.kotlin.dsl.converter

import kotlin.reflect.KClass

enum class Flavor(val converterClass: KClass<out Converter>) {
  AllOptional(AllOptionalConverter::class),
  RequiredPrimaryConstructor(RequiredPrimaryConstructorConverter::class),
  ;

  companion object {
    val DEFAULT = AllOptional
  }
}

fun Flavor.create(): Converter = converterClass.java.getDeclaredConstructor().newInstance()
