package pub.keil.kotlin.dsl.converter

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import pub.keil.kotlin.dsl.model.DslClass

class AllOptionalConverter : Converter {
  override fun convert(dslClass: DslClass): FileSpec {
    val className = ClassName(dslClass.packageName, dslClass.name)
    val builder = TypeSpec.classBuilder(dslClass.name)
    val properties = dslClass.properties.map(::toPropertySpec)
    if (properties.isNotEmpty()) {
      builder.addType(createInvokeCompanionFunction(className))
      builder.addProperties(properties)
    } else {
      builder.addType(createCompanionObject())
    }
    dslClass.description?.let { description -> builder.addKdoc(escapeDoc(description)) }
    return FileSpec.builder(className).addType(builder.build()).build()
  }

  private fun createCompanionObject(): TypeSpec {
    return TypeSpec.companionObjectBuilder().build()
  }

  private fun createInvokeCompanionFunction(self: TypeName): TypeSpec {
    return TypeSpec.companionObjectBuilder()
        .addFunction(
            FunSpec.builder("invoke")
                .addParameter(
                    "block",
                    LambdaTypeName.get(
                        receiver = self,
                        returnType = UNIT,
                    ))
                .addModifiers(KModifier.OPERATOR)
                .returns(self)
                .addStatement("return %T().apply(block)", self)
                .build())
        .build()
  }
}
