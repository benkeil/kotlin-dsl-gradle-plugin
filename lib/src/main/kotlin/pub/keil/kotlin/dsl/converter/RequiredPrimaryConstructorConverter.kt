package pub.keil.kotlin.dsl.converter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import pub.keil.kotlin.dsl.model.DslClass
import pub.keil.kotlin.dsl.model.DslProperty

class RequiredPrimaryConstructorConverter : Converter {
  override fun convert(dslClass: DslClass): FileSpec {
    val className = ClassName(dslClass.packageName, dslClass.name)
    val builder = TypeSpec.classBuilder(dslClass.name)
    val (requiredProps, optionalProps) = dslClass.properties.partition { it.required }
    val requiredProperties = requiredProps.map(::toPropertySpec)
    val optionalProperties = optionalProps.map(::toPropertySpec)
    if (requiredProperties.isNotEmpty()) {
      builder
          .primaryConstructor(getPrimaryConstructor(requiredProps))
          .addProperties(getRequiredPropertiesForPrimaryConstructor(requiredProps))
    }
    if (optionalProperties.isNotEmpty()) {
      builder
          .addType(createInvokeCompanionFunction(className, requiredProps))
          .addProperties(optionalProperties)
    } else {
      builder.addType(createCompanionObject())
    }
    dslClass.description?.let { description -> builder.addKdoc(escapeDoc(description)) }

    return FileSpec.builder(className).addType(builder.build()).build()
  }

  private fun getPrimaryConstructor(requiredProperties: List<DslProperty>): FunSpec {
    val parameters = requiredProperties.map { ParameterSpec.builder(it.name, it.type).build() }
    return FunSpec.constructorBuilder().addParameters(parameters).build()
  }

  private fun getRequiredPropertiesForPrimaryConstructor(
      requiredProperties: List<DslProperty>
  ): List<PropertySpec> {
    return requiredProperties.map { property ->
      PropertySpec.builder(property.name, property.type)
          .mutable()
          .also { property.description?.let { description -> it.addKdoc(escapeDoc(description)) } }
          .initializer(property.name)
          .build()
    }
  }

  private fun createCompanionObject(): TypeSpec {
    return TypeSpec.companionObjectBuilder().build()
  }

  private fun createInvokeCompanionFunction(
      self: TypeName,
      requiredProperties: List<DslProperty>
  ): TypeSpec {
    val requiredParameters =
        requiredProperties.map { ParameterSpec.builder(it.name, it.type).build() }
    val template = requiredProperties.map { it.name }.joinToString(", ") { it }
    return TypeSpec.companionObjectBuilder()
        .addFunction(
            FunSpec.builder("invoke")
                .addParameters(requiredParameters)
                .addParameter(
                    "block",
                    LambdaTypeName.get(
                        receiver = self,
                        returnType = UNIT,
                    ))
                .addModifiers(KModifier.OPERATOR)
                .returns(self)
                .addStatement("return %T($template).apply(block)", self)
                .build())
        .build()
  }
}
