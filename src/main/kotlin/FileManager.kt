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
import java.lang.Double.sum

class FileManager {
    val path = "/Users/runbyzo/Documents/programs/Kotlin/travel_agency/src/resources"
    var jsonFile = File("")

    constructor(name: String){
        this.jsonFile = File("$path/$name.json")
    }

    private fun readJsonArray(): JsonArray {
        if (!jsonFile.exists() || jsonFile.readText().isBlank()) return JsonArray(emptyList())
        return runCatching {
            Json.parseToJsonElement(jsonFile.readText()).jsonArray
        }.getOrElse { e ->
            println("Failed to parse JSON: ${e.message}")
            JsonArray(emptyList())
        }
    }

    // method what reads file
    fun loadFromFile(): String {
        if (!jsonFile.exists()) return "[]".also { println("File not found: ${jsonFile.path}") }
        return runCatching { jsonFile.readText() }
            .getOrElse { e -> "[]".also { println("Failed to read file: ${e.message}") } }
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
        hotelName: String
    ): Tour {
        val record = Tour(recordName, startDate, endDate, isActive, price, description, hotelName)
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

    fun find(recordName: String): List<JsonEntity> {
        val dataFile = runCatching { readJsonArray() }.getOrElse {
            println("Could not read file")
            return emptyList()
        }

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
                runCatching {
                    when {
                        element.containsKey("startDate") -> Json.decodeFromJsonElement<Tour>(element)
                        element.containsKey("location")  -> Json.decodeFromJsonElement<Hotel>(element)
                        else -> null.also { println("Skipping unknown element: $element") }
                    }
                }.getOrElse { e -> null.also { println("Failed to decode element: ${e.message}") } }
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
        val existingArray = readJsonArray()

        val filtered = existingArray.filter { element ->
            (element as? JsonObject)?.get("name")?.toString()?.trim('"') != record.name
        }

        val newElement = when (record) {
            is Hotel -> json.encodeToJsonElement(record)
            is Tour  -> json.encodeToJsonElement(record)
            else     -> return println("Unsupported record type")
        }

        runCatching {
            jsonFile.writeText(json.encodeToString(JsonArray(filtered + newElement)))
            println("\nRecord was saved in: ${jsonFile.path}")
        }.onFailure { e -> println("Failed to save record: ${e.message}") }
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

        print("patch hotel name? (yes to change): ")
        val patchedHotel = readOrDefault() ?: tour.hotelName

        saveRecord(Tour(patchedName, patchedStartDate, patchedEndDate, patchedIsActive, patchedPrice, patchedDescription, patchedHotel))
    }

    fun patchRecord(recordName: String) {
        val file = runCatching { jsonFile.readText() }.getOrElse {
            println("Could not read file"); return
        }
        val jsonArray = runCatching { Json.parseToJsonElement(file).jsonArray }.getOrElse {
            println("Invalid JSON in file"); return
        }

        if (recordName !in file) { println("Record not found"); return }

        val matchingElement = jsonArray
            .filterIsInstance<JsonObject>()
            .find { it["name"]?.toString()?.trim('"') == recordName }
            ?: return println("Record not found")

        val record: JsonEntity = runCatching {
            when {
                matchingElement.containsKey("startDate") -> Json.decodeFromJsonElement<Tour>(matchingElement)
                matchingElement.containsKey("location")  -> Json.decodeFromJsonElement<Hotel>(matchingElement)
                else -> return println("Unsupported record type")
            }
        }.getOrElse { e -> return println("Failed to decode record: ${e.message}") }

        when (record) {
            is Hotel -> patchHotel(record, recordName)
            is Tour  -> patchTour(record, recordName)
        }
    }

    fun deleteRecord(recordName: String) {
        val json = Json { prettyPrint = true }
        val jsonArray = readJsonArray()

        if (recordName !in jsonFile.readText()) {
            println("Record not found")
            return
        }

        val filtered = JsonArray(
            jsonArray.filter { element ->
                (element as? JsonObject)?.get("name")?.toString()?.trim('"') != recordName
            }
        )

        runCatching {
            jsonFile.writeText(json.encodeToString(filtered))
            println("\nRecord '$recordName' was deleted from: ${jsonFile.path}")
        }.onFailure { e -> println("Failed to delete record: ${e.message}") }
    }

    fun sortFile(field: String) {
        val json = Json { prettyPrint = true }
        val jsonArray = readJsonArray()

        val sorted = when (field) {
            "name", "location", "description", "startDate", "endDate", "hotelName" ->
                jsonArray.sortedBy { (it as? JsonObject)?.get(field)?.toString()?.trim('"') }
            "stars" ->
                jsonArray.sortedBy { (it as? JsonObject)?.get(field)?.toString()?.toIntOrNull() }
            "price" ->
                jsonArray.sortedBy { (it as? JsonObject)?.get(field)?.toString()?.toDoubleOrNull() }
            else -> return println("$field is unsupported field")
        }

        runCatching {
            jsonFile.writeText(json.encodeToString(JsonArray(sorted)))
            println("\nFile sorted by '$field' and saved")
        }.onFailure { e -> println("Failed to sort file: ${e.message}") }
    }

    fun getStats() {
        val jsonArray = Json.parseToJsonElement(jsonFile.readText()).jsonArray
        val fileText = jsonFile.readText()

        println("Current stats " +
                "\n-----------------------" +
                "\nname: ${jsonFile.name}" +
                "\nnumber of elements: ${jsonArray.size}")

        when {
            "location" in fileText -> {
                println("element type: Hotel" +
                        "\nWorst hotel: ${findMin("stars")?.get("name")?.toString()}" +
                        "\nBest hotel: ${findMax("stars")?.get("name")?.toString()}")
            }
            "startDate" in fileText -> {
                println("element type: Tour" +
                        "\nCheapest tour: ${findMin("price")?.get("name")?.toString()}" +
                        "\nMost expensive: ${findMax("price")?.get("name")?.toString()}")
            }
            else -> println("unknown element type")
        }
    }


    fun findMin(field: String): JsonObject? {
        val jsonArray = Json.parseToJsonElement(jsonFile.readText()).jsonArray

        val result = when (field) {
            "stars" -> jsonArray.minByOrNull { element ->
                (element as? JsonObject)?.get(field)?.toString()?.toIntOrNull() ?: 0
            }
            "price" -> jsonArray.minByOrNull { element ->
                (element as? JsonObject)?.get(field)?.toString()?.toDoubleOrNull() ?: 0.0
            }
            else -> return println("$field is unsupported field").let { null }
        }

        return result as? JsonObject
    }

    fun findMax(field: String): JsonObject? {
        val jsonArray = Json.parseToJsonElement(jsonFile.readText()).jsonArray

        val result = when (field) {
            "stars" -> jsonArray.maxByOrNull { element ->
                (element as? JsonObject)?.get(field)?.toString()?.toIntOrNull() ?: 0
            }
            "price" -> jsonArray.maxByOrNull { element ->
                (element as? JsonObject)?.get(field)?.toString()?.toDoubleOrNull() ?: 0.0
            }
            else -> return println("$field is unsupported field").let { null }
        }

        return result as? JsonObject
    }
}

