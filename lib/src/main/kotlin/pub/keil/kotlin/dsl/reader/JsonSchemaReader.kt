package pub.keil.kotlin.dsl.reader

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SET
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Path
import kotlin.io.path.Path
import pub.keil.kotlin.dsl.converter.AllOptionalConverter
import pub.keil.kotlin.dsl.extension.capitalize
import pub.keil.kotlin.dsl.model.DslClass
import pub.keil.kotlin.dsl.model.DslProperty
import pub.keil.kotlin.dsl.utils.getResourceAsPath

private val logger = KotlinLogging.logger {}
private val mapper = jacksonObjectMapper().registerModules(JavaTimeModule())
private val debugMapper = mapper.writerWithDefaultPrettyPrinter()

class JsonSchemaReader(
    private val aliases: Map<String, String> = emptyMap(),
    private val propertiesToSkip: List<String> = emptyList(),
) {

  private val classes: MutableList<DslClass> = mutableListOf()

  fun read(canonicalName: String, from: Path): List<DslClass> = read(canonicalName, from.toFile())

  fun read(canonicalName: String, from: File): List<DslClass> = read(canonicalName, from.readText())

  fun read(canonicalName: String, schema: String): List<DslClass> =
      read(canonicalName, mapper.readTree(schema))

  fun read(canonicalName: String, node: JsonNode): List<DslClass> {
    val basePackageName = canonicalName.substringBeforeLast('.')
    val className = canonicalName.substringAfterLast('.')
    try {
      readObject(basePackageName, className, node)
      return classes
    } catch (e: Exception) {
      throw RuntimeException("Failed to read object $basePackageName.$className", e)
    }
  }

  private fun readObject(packageName: String, className: String, node: JsonNode) {
    logger.debug { "read object $packageName.$className" }
    logger.trace { "read object $packageName.$className $node" }
    classes +=
        DslClass(
            packageName = packageName,
            name = className,
            description = node["description"]?.asText(),
            properties = getProperties(packageName, node),
        )
  }

  private fun getProperties(packageName: String, node: JsonNode): List<DslProperty> {
    logger.trace { "read properties $node" }
    val properties =
        (node["properties"]
            ?.fields()
            ?.asSequence()
            ?.filter { !propertiesToSkip.contains(it.key) }
            ?.toList() ?: emptyList())
    logger.debug { "properties ${properties.map { it.key }}" }
    logger.trace { "properties $properties" }
    val required = node["required"]?.map { it.asText() } ?: emptyList()
    return properties.map {
      try {
        val type = getTypeName(packageName, it.key, it.value).applyAliases()
        DslProperty(
            name = it.key,
            type = type,
            description = it.value["description"]?.asText(),
            required = required.contains(it.key),
            isObject = it.value["type"]?.asText() == "object",
        )
      } catch (e: Exception) {
        throw RuntimeException("Failed to get properties of ${it.key}", e)
      }
    }
  }

  private fun getTypeName(packageName: String, className: String, node: JsonNode): TypeName {
    try {
      logger.debug { "get type of $packageName.$className" }
      logger.trace { "get type of $packageName.$className $node" }
      val type = node["type"]?.asText()
      val format = node["format"]?.asText()
      val ref = node["\$ref"]?.asText()
      return when {
        ref != null -> {
          val definition = ref.substringAfterLast("/")
          val refPackageName = definition.substringBeforeLast(".")
          val refClassName = definition.substringAfterLast(".")
          ClassName(refPackageName, refClassName)
        }
        type == "string" -> STRING
        type == "integer" && format == "int64" -> LONG
        type == "number" && format == "double" -> DOUBLE
        type == "integer" -> INT
        type == "boolean" -> BOOLEAN
        type == "object" && node["properties"] != null -> {
          val nestedKey = className.capitalize()
          readObject(packageName, nestedKey, node)
          return ClassName(packageName, nestedKey)
        }
        type == "object" && node["additionalProperties"] != null ->
            MAP.parameterizedBy(
                STRING, getTypeName(packageName, className, node["additionalProperties"]))
        type == "array" -> {
          val listType = node["x-kubernetes-list-type"]?.asText()
          val singularClassName = className.substringBeforeLast("s").capitalize()
          if (listType == "set") {
            SET.parameterizedBy(getTypeName(packageName, singularClassName, node["items"]))
          } else {
            LIST.parameterizedBy(getTypeName(packageName, singularClassName, node["items"]))
          }
        }
        else -> error("Unknown type: $type")
      }
    } catch (e: Exception) {
      throw RuntimeException("Failed to get type of $packageName.$className", e)
    }
  }

  private fun TypeName.applyAliases(): TypeName {
    val alias = aliases["$this"]
    return if (alias != null) {
      ClassName.bestGuess(alias)
    } else {
      this
    }
  }
}

fun main() {
  val converter = AllOptionalConverter()
  JsonSchemaReader()
      .read("de.otto.pdh.da.aws.ecs.TaskDefinition", getResourceAsPath("task-definition.json"))
      .map(converter::convert)
      .forEach { it.writeTo(Path("lib/build/generated/sources/dsl")) }
}
