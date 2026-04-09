package ex

import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

const val version = "0.0"

fun main() {
    val path = "/Users/runbyzo/Documents/programs/Kotlin/travel_agency/src/resources"
    println("Travel Agency $version is getting started!")

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
                File("${"$path/$name"}.json").writeText("{}")
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

fun sideMenu(name: String){
    val fileManager = FileManager(name)

    while (true){
        println("""
            
           Available JSON-operations: 
            1. Show all records.
            2. Add dynamic record.
            3. Редактирование существующей записи по идентификатору.
            4. Удаление записи.
            5. Поиск записей (минимум по одному текстовому полю).
            6. Сортировка записей (минимум по одному числовому или текстовому полю).
            7. Вычисление агрегированного показателя (среднее, сумма, минимум, максимум).
            8. Сохранение данных в JSON-файл.
            9. Выход
             
        """.trimIndent())

        print("-> ")
        when (val input = readlnOrNull()?.toInt()) {
            1 -> println(fileManager.loadFromFile())
            2 -> {
                print("Input name of record: ")
                val recordName = readln().trim()

                print("How many fields? ")
                val count = readln().trim().toInt()

                val fields = LinkedHashMap<String, JsonElement>()

                repeat(count) { i ->
                    println("\nField ${i + 1}:")

                    print("\nField Name: ")
                    val fieldName = readln().trim()

                    print("\nType (1=String, 2=Int, 3=Double, 4=Boolean): ")
                    val type = readln().trim()

                    print("\nValue: ")
                    val rawValue = readln().trim()

                    fields[fieldName] = when (type) {
                        "2" -> JsonPrimitive(rawValue.toInt())
                        "3" -> JsonPrimitive(rawValue.toDouble())
                        "4" -> JsonPrimitive(rawValue.toBooleanStrict())
                        else -> JsonPrimitive(rawValue)
                    }
                }
                fileManager.buildAndSaveRecord(recordName, count, fields)
            }
            3 -> println("...")
            4 -> println("...")
            5 -> println("...")
            6 -> println("...")
            7 -> println("...")
            8 -> println("...")
            9 -> break
            else -> println("wrong input: $input wasn't identified")
        }
    }
}