package pub.keil.kotlin.dsl.reader

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import pub.keil.kotlin.dsl.model.DslClass
import java.io.File
import java.nio.file.Path

private val mapper = jacksonObjectMapper().registerModules(JavaTimeModule())
private val debugMapper = mapper.writerWithDefaultPrettyPrinter()

class OpenApiReader(
    private val jsonSchemaReader: JsonSchemaReader,
) {
  fun read(from: Path): List<DslClass> = read(from.toFile())

  fun read(from: File): List<DslClass> = read(from.readText())

  fun read(schema: String): List<DslClass> = read(mapper.readTree(schema))

  fun read(node: JsonNode): List<DslClass> =
      node
          .path("definitions")
          .fields()
          .asSequence()
          .flatMap {
            try {
              jsonSchemaReader.read(it.key, it.value)
            } catch (e: Exception) {
              throw RuntimeException("Failed to read definition ${it.key}", e)
            }
          }
          .toList()
}
