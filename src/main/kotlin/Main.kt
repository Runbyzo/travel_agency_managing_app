package ex

import ex.entities.Hotel
import ex.entities.Meal
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val version = "0.1"

fun main() {
    // Main function. This function starts entier app. Also here is interfaces of app
    val path = "/Users/runbyzo/Documents/programs/Kotlin/travel_agency/src/resources"
    val today = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    println("Travel Agency $version is getting started!")

    // Main menu. here you can make new file, open existing and delete one of them.
    while (true){
        val resources = File(path).list()

        println("""
            
            Меню:
             1. Create new file
             2. Open existing file
             3. Delete existing file
             4. Exit
             
        """.trimIndent())

        print("-> ")
        when (val input = readlnOrNull()?.toInt()) {
            1 -> {
                print("Name of new file (default 'noname file') -> ")
                val name = readlnOrNull() ?: "noname file"
                File("${"$path/$name"}.json").writeText("[]")
                println("File $name was created")
            }
            2 -> {
                println("Openable files:")
                if (resources != null) {
                    resources.forEach { println("\t" + it)}
                }
                println("")
                print("Input file name which you want to open (default 'noname file') -> ")
                sideMenu(readlnOrNull() ?: "noname file")
            }
            3 -> {
                println("Deletable files:")

                if (resources != null) {
                    resources.forEach { println("\t" + it)}
                }

                print("Input file name which you want to delete (default 'noname file') -> ")

                val file = File("$path/${readlnOrNull() ?: "noname file"}.json")
                val deleted = file.delete()

                if (deleted) {
                    println("File deleted!")
                } else {
                    println("File not found or could not be deleted")
                }
            }
            4 -> break
            else -> println("Wrong input: $input wasn't identified")
        }
    }
}

// menu for json file handling
fun sideMenu(name: String){
    val fileManager = FileManager(name)

    while (true){
        println("""
            
           Available JSON-operations: 
            1. Show all records
            2. Add dynamic record (you won't be able to patch it)
            3. Add tour record
            4. Add hotel record
            5. Patch record
            6. Delete record (you won't be able to patch it) <3
            7. Search record
            8. Sort records
            9. statistics of file
            10. Выход
             
        """.trimIndent())

        print("-> ")
        when (val input = readlnOrNull()?.toInt()) {
            1 -> println(fileManager.loadFromFile()) // Show all records
            2 -> {
                // Add dynamic record
                print("Input name of record: ")
                val recordName = readln().trim()

                print("How many fields? ")
                val count = readln().trim().toInt()

                val fields = LinkedHashMap<String, JsonElement>()

                // this algorithm makes fields of dynamic class to create any json elements
                repeat(count) { i ->
                    println("\nField ${i + 1}:")

                    print("\nField Name: ")
                    val fieldName = readlnOrNull()?.trim() ?: "unnamed field"

                    print("\nType (1=String, 2=Int, 3=Double, 4=Boolean): ")
                    val type = readlnOrNull()?.trim() ?: 1

                    print("\nValue: ")
                    val rawValue = readln().trim()

                    fields[fieldName] = when (type) {
                        "2" -> JsonPrimitive(rawValue.toInt())
                        "3" -> JsonPrimitive(rawValue.toDouble())
                        "4" -> JsonPrimitive(rawValue.toBooleanStrict())
                        else -> JsonPrimitive(rawValue)
                    }
                }
                fileManager.buildAndSaveDRecord(recordName, count, fields)
            }
            3 -> {
                // Add tour record

                print("Name of record: ")
                val recordName = readlnOrNull()?.trim() ?: "unnamed record"

                print("how many days will get this tour: ")
                val endDate: Long = readlnOrNull()?.toLong() ?: 7

                print("Is tour active (true/false): ")
                val isActive = readlnOrNull()?.toBoolean() ?: false

                print("Price: ")
                val price = readlnOrNull()?.toDouble() ?: 0.0

                print("Description: ")
                val description = readlnOrNull()?.trim() ?: "there is nothing"

                print("hotel name: ")
                val hotelName = readlnOrNull()?.trim() ?: "unnamed hotel"

                fileManager.buildAndSaveTRecord(
                    recordName = recordName,
                    startDate = LocalDateTime.now().toString(),
                    endDate = LocalDateTime.now().plusDays(endDate).toString(),
                    price = price,
                    description = description,
                    isActive = isActive,
                    hotelName = hotelName)
            }
            4 -> {
                // ask about hotel params
                print("hotel name: ")
                val hotelName = readlnOrNull()?.trim() ?: "unnamed hotel"

                print("location: ")
                val location = readlnOrNull()?.trim() ?: "unnamed location"

                print("description: ")
                val hotelDescription = readlnOrNull()?.trim() ?: "there is nothing"

                print("Meal types (write them by using ','), (RoomOnly, AllInclusive, BedBreakfast, HalfBoard, FullBoard): ")
                val mealTypes = readlnOrNull()?.split(',')
                val mealPlan  = convertToEnums(mealTypes)

                print("how many stars: ")
                val stars = readlnOrNull()?.toInt() ?: 0

                print("is hotel available (true/false): ")
                val isHotelAvailable = readlnOrNull()?.toBoolean() ?: false

                fileManager.buildAndSaveHRecord(
                    recordName = hotelName,
                    location = location,
                    description = hotelDescription,
                    mealPlan = mealPlan,
                    stars = stars,
                    isAvailable = isHotelAvailable
                )
            }
            5 -> {
                print("record name: ")
                val recordName = readlnOrNull()?.trim() ?: "unnamed record"
                fileManager.patchRecord(recordName)
            }
            6 -> {
                println("record name: ")
                val recordName = readlnOrNull()?.trim() ?: "unnamed record"
                print(fileManager.deleteRecord(recordName))
            }
            7 -> {
                println("record name: ")
                val recordName = readlnOrNull()?.trim() ?: "unnamed record"
                println(fileManager.find(recordName))
            }
            8 -> {
                println("sort by field: ")
                val recordName = readlnOrNull()?.trim() ?: "unnamed record"
                fileManager.sortFile(recordName)
            }
            9 -> {
                fileManager.getStats()
            }
            10 -> break
            else -> println("wrong input: $input wasn't identified")
        }
    }
}

// this method converts list of strings to list of enums
fun convertToEnums(inputs: List<String>?): MutableList<Meal?> {
    val result = mutableListOf<Meal?>()
    for (i in inputs!!) {
        for (j in Meal.entries) {
            if (i == j.toString()) result.add(j)
        }
    }
    return result
}
