package com.koofree.utils

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

inline fun <reified T> T.toJson(): String = Json.encodeToString(this)

fun Map<String, *>.toJson(): String = buildJsonObject {
    forEach { (key, value) ->
        if (value is Number)
            put(key, value.toLong())
        else
            put(key, value.toString())
    }
}.toString()

fun String.toJsonMap(): Map<String, Any> = Json.decodeFromString(JsonMapDeserializationStrategy, this)

object JsonMapDeserializationStrategy : DeserializationStrategy<Map<String, Any>> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("JsonMap")

    override fun deserialize(decoder: Decoder): Map<String, Any> {
        val input = decoder as? JsonDecoder
            ?: throw SerializationException("Expected Json Input")
        return when (val jsonElement = input.decodeJsonElement()) {
            is JsonArray -> {
                jsonElement.map { element ->
                    element as JsonObject
                    val firstKey = element.keys.first()
                    firstKey to element[firstKey]!!.jsonPrimitive.let { primitive ->
                        primitive.longOrNull ?: primitive.content
                    }
                }.toMap()
            }
            is JsonObject -> {
                jsonElement.map { (k, v) -> k to v.jsonPrimitive.content }.toMap()
            }
            else -> throw SerializationException("Expected JsonArray")
        }
    }
}
