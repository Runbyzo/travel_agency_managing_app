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
                            mealPlan: MutableList<Meal?>,
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

        // Remove the old record with the same name before appending the updated one
        val filtered = existingArray.filter { element ->
            (element as? JsonObject)?.get("name")?.toString()?.trim('"') != record.name
        }

        val newElement = json.encodeToJsonElement(record)
        val updatedArray = JsonArray(filtered + newElement)

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

        val filtered = existingArray.filter { element ->
            (element as? JsonObject)?.get("name")?.toString()?.trim('"') != record.name
        }

        val newElement = json.encodeToJsonElement(record)
        val updatedArray = JsonArray(filtered + newElement)

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

    fun find(recordName: String): List<JsonEntity> {
        val dataFile = Json.parseToJsonElement(jsonFile.readText()).jsonArray

        fun levenshtein(a: String, b: String): Int {
            val dp = Array(a.length + 1) { IntArray(b.length + 1) }
            for (i in 0..a.length) dp[i][0] = i
            for (j in 0..b.length) dp[0][j] = j
            for (i in 1..a.length) {
                for (j in 1..b.length) {
                    dp[i][j] = if (a[i-1] == b[j-1]) dp[i-1][j-1]
                    else 1 + minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
                }
            }
            return dp[a.length][b.length]
        }

        val records = dataFile
            .filterIsInstance<JsonObject>()
            .mapNotNull { element ->
                when {
                    element.containsKey("startDate") ->
                        Json.decodeFromJsonElement<Tour>(element)
                    element.containsKey("location") ->
                        Json.decodeFromJsonElement<Hotel>(element)
                    else -> null
                }
            }

        if (records.isEmpty()) {
            println("No records found")
            return emptyList()
        }

        val results = records
            .sortedBy { levenshtein(it.name.lowercase(), recordName.lowercase()) }
            .take(5)

        println("Top ${results.size} most similar records to '$recordName':")
        results.forEachIndexed { i, record ->
            println("  ${i + 1}. ${record.name} (${if (record is Hotel) "Hotel" else "Tour"})")
        }

        return results
    }

    private fun saveRecord(record: JsonEntity) {
        val json = Json { prettyPrint = true }

        val existingArray: JsonArray = if (jsonFile.exists() && jsonFile.readText().isNotBlank()) {
            json.parseToJsonElement(jsonFile.readText()).jsonArray
        } else {
            JsonArray(emptyList())
        }

        val filtered = existingArray.filter { element ->
            (element as? JsonObject)?.get("name")?.toString()?.trim('"') != record.name
        }

        val newElement = when (record) {
            is Hotel -> json.encodeToJsonElement(record)
            is Tour  -> json.encodeToJsonElement(record)
            else     -> return println("Unsupported record type")
        }

        jsonFile.writeText(json.encodeToString(JsonArray(filtered + newElement)))
        println("\nRecord was saved in: ${jsonFile.path}")
    }


    private fun readOrDefault(): String? = readlnOrNull()?.trim()?.ifBlank { null }

    private fun patchHotel(hotel: Hotel, recordName: String): Hotel {
        println("Patching hotel $recordName")

        print("new name (old: ${hotel.name}): ")
        val patchedName = readOrDefault() ?: hotel.name

        print("new location (old: ${hotel.location}): ")
        val patchedLocation = readOrDefault() ?: hotel.location

        print("new description (old: ${hotel.description}): ")
        val patchedDescription = readOrDefault() ?: hotel.description

        print("new meal plan, comma-separated (','), (RoomOnly, AllInclusive, BedBreakfast, HalfBoard, FullBoard), (old: ${hotel.mealPlan}): ")
        val patchedMealPlan = readOrDefault()?.split(',')?.let { convertToEnums(it) } ?: hotel.mealPlan

        print("new stars (old: ${hotel.stars}): ")
        val patchedStars = readOrDefault()?.toIntOrNull() ?: hotel.stars

        print("new isAvailable true/false (old: ${hotel.isAvailable}): ")
        val patchedIsAvailable = readOrDefault()?.toBooleanStrictOrNull() ?: hotel.isAvailable

        val patched = Hotel(patchedName, patchedLocation, patchedDescription, patchedMealPlan, patchedStars, patchedIsAvailable)
        saveRecord(patched)
        return patched
    }

    private fun patchTour(tour: Tour, recordName: String) {
        println("Patching tour $recordName")

        print("new name (old: ${tour.name}): ")
        val patchedName = readOrDefault() ?: tour.name

        print("new start date (old: ${tour.startDate}): ")
        val patchedStartDate = readOrDefault() ?: tour.startDate

        print("new end date (old: ${tour.endDate}): ")
        val patchedEndDate = readOrDefault() ?: tour.endDate

        print("new isActive true/false (old: ${tour.isActive}): ")
        val patchedIsActive = readOrDefault()?.toBooleanStrictOrNull() ?: tour.isActive

        print("new price (old: ${tour.price}): ")
        val patchedPrice = readOrDefault()?.toDoubleOrNull() ?: tour.price

        print("new description (old: ${tour.description}): ")
        val patchedDescription = readOrDefault() ?: tour.description

        print("patch hotel? (yes to change): ")
        val patchedHotel = if (readOrDefault() == "yes") {
            patchHotel(tour.hotel ?: return, tour.name)
        } else {
            tour.hotel
        }

        saveRecord(Tour(patchedName, patchedStartDate, patchedEndDate, patchedIsActive, patchedPrice, patchedDescription, patchedHotel))
    }

    fun patchRecord(recordName: String) {
        val file = jsonFile.readText()
        val jsonArray = Json.parseToJsonElement(file).jsonArray

        if (recordName !in file) {
            println("Record not found")
            return
        }

        val matchingElement = jsonArray
            .filterIsInstance<JsonObject>()
            .find { it["name"]?.toString()?.trim('"') == recordName }
            ?: return println("Record not found")

        // Detect type by checking which fields are present
        val record: JsonEntity = when {
            matchingElement.containsKey("startDate") ->
                Json.decodeFromJsonElement<Tour>(matchingElement)
            matchingElement.containsKey("location") ->
                Json.decodeFromJsonElement<Hotel>(matchingElement)
            else -> return println("Unsupported record type")
        }

        when (record) {
            is Hotel -> patchHotel(record, recordName)
            is Tour  -> patchTour(record, recordName)
        }
    }

    fun deleteRecord(recordName: String) {
        val file = jsonFile.readText()
        val json = Json { prettyPrint = true }
        val jsonArray = Json.parseToJsonElement(file).jsonArray

        if (recordName !in file) {
            println("Record not found")
            return
        }

        val filtered = JsonArray(
            jsonArray.filter { element ->
                (element as? JsonObject)?.get("name")?.toString()?.trim('"') != recordName
            }
        )

        jsonFile.writeText(json.encodeToString(filtered))
        println("\nRecord '$recordName' was deleted from: ${jsonFile.path}")
    }

}

