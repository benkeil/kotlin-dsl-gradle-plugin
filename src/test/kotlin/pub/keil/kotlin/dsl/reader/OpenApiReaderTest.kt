package pub.keil.kotlin.dsl.reader

import io.kotest.core.spec.style.FunSpec
import kotlin.io.path.Path
import pub.keil.kotlin.dsl.converter.AllOptionalConverter
import pub.keil.kotlin.dsl.utils.getResourceAsPath

class OpenApiReaderTest :
    FunSpec({
      xtest("read") {
        val converter = AllOptionalConverter()
        val jsonSchemaReader =
            JsonSchemaReader(
                propertiesToSkip = listOf("apiVersion", "kind", "status"),
                aliases =
                    mapOf("io.k8s.apimachinery.pkg.util.intstr.IntOrString" to "kotlin.String"))
        OpenApiReader(jsonSchemaReader = jsonSchemaReader)
            .read(getResourceAsPath("kubernetes.json"))
            .map(converter::convert)
            .forEach { it.writeTo(Path("lib/build/generated/sources/dsl")) }
      }
    })
