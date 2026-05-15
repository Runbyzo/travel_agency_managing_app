import java.io.File

fun sum(): Int {
    val file = File("example.txt")

    if (!file.exists()) {
        println("Could not open file")
        return 1
    }


    val result = 0
    file.readLines().forEach { line -> line.toInt() + result }
    return result
}


fun main(){
}