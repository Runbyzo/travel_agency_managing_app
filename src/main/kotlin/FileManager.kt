package ex

import DynamicRecord
import ex.entities.Hotel
import ex.entities.JsonEntity
import ex.entities.Meal
import ex.entities.Tour
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import java.io.File


class FileManager {
    val path = "/Users/runbyzo/Documents/programs/Kotlin/travel_agency/src/resources"
    var jsonFile = File("")

    constructor(name: String){
        this.jsonFile = File("$path/$name.json")
    }

    // method what reads file
    fun loadFromFile(): String {
        val jsonString = jsonFile.readText()
        return jsonString
    }

    fun buildAndSaveDRecord(recordName: String,
                            count: Int,
                            fields: LinkedHashMap<String, JsonElement>): DynamicRecord {
        val record = DynamicRecord(name = recordName, fields = fields)
        saveDRecord(record)
        return record
    }

    fun buildAndSaveTRecord(
        recordName: String,
        startDate: String,
        endDate: String,
        isActive: Boolean,
        price: Double,
        description: String,
        hotel: Hotel?
    ): Tour {
        val record = Tour(recordName, startDate, endDate, isActive, price, description, hotel)
        saveTRecord(record)
        return record
    }

    fun buildAndSaveHRecord(recordName: String,
                            location: String,
                            description: String,
                            mealPlan: List<Meal>,
                            stars: Int,
                            isAvailable: Boolean): Hotel {
        val record = Hotel(recordName, location, description, mealPlan, stars, isAvailable)
        saveHRecord(record)
        return record
    }

    private fun saveDRecord(record: DynamicRecord) {
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

    private fun saveTRecord(record: Tour) {
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

    private fun saveHRecord(record: Hotel) {
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

    fun findHotel(hotelName: String): Hotel? {
        val hotelsJson = File("$path/hotels.json").readText()
        val jsonArray = Json.parseToJsonElement(hotelsJson).jsonArray
        val result: Hotel?

        if(hotelName in hotelsJson){
            println("start searching for hotel $hotelName")
        } else {
            println("hotel does not exist")
            return null
        }

        val hotels = jsonArray.map {
            Json.decodeFromJsonElement<Hotel>(it)
        }

        return hotels.find { it.name == hotelName}
    }

    fun patchRecord(recordName: String) {
        val file = jsonFile.readText()
        val jsonArray = Json.parseToJsonElement(file).jsonArray

        if(recordName in file){
            val records = jsonArray.map {
                Json.decodeFromJsonElement<JsonEntity>(it)
            }
            val record = records.find { it.name == recordName }
            println("record found: $record")
        }
        else{
            println("record not found")
        }

        // ... <-- here i stopped i have to make this part what patches record
    }

}
// iron-quake