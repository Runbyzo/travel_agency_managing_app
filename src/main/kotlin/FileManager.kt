package ex

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Serializable
data class DynamicRecord(
    val name: String,
    val fields: LinkedHashMap<String, JsonElement>
)

class FileManager {
    val path = "/Users/runbyzo/Documents/programs/Kotlin/travel_agency/src/resources"
    var jsonFile = File("")
    val today = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    constructor(name: String){
        this.jsonFile = File("$path/$name.json")
    }

    // method what reads file
    fun loadFromFile(): String {
        val jsonString = jsonFile.readText()
        return jsonString
    }

    fun buildAndSaveRecord(recordName: String, count: Int, fields: LinkedHashMap<String, JsonElement>): DynamicRecord {
        val record = DynamicRecord(name = recordName, fields = fields)
        saveRecord(record)
        return record
    }

    private fun saveRecord(record: DynamicRecord) {
        val json = Json { prettyPrint = true }

        val existingArray: JsonArray = if (jsonFile.exists() && jsonFile.readText().isNotBlank()) {
            json.parseToJsonElement(jsonFile.readText()).jsonArray
        } else {
            JsonArray(emptyList())
        }

        val newElement = json.encodeToJsonElement(record)
        val updatedArray = JsonArray(existingArray + newElement)

        jsonFile.writeText(json.encodeToString(updatedArray))
        println("\nRecord was saved in: ${jsonFile.path}")
    }
}
