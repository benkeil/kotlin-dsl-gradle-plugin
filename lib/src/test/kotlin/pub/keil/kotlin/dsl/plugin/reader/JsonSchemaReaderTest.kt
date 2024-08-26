package pub.keil.kotlin.dsl.plugin.reader

import io.kotest.core.spec.style.FunSpec
import kotlin.io.path.Path
import pub.keil.kotlin.dsl.converter.Flavor
import pub.keil.kotlin.dsl.converter.create
import pub.keil.kotlin.dsl.reader.JsonSchemaReader
import pub.keil.kotlin.dsl.utils.getResourceAsPath

class JsonSchemaReaderTest :
    FunSpec({
      test("read") {
        val converter = Flavor.DEFAULT.create()
        JsonSchemaReader()
            .read(
                "de.otto.pdh.da.aws.ecs.TaskDefinition", getResourceAsPath("task-definition.json"))
            .map(converter::convert)
            .forEach { it.writeTo(Path("lib/build/generated/sources/kotlin-dsl")) }
      }
    })
