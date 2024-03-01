import app.App
import app.data.postgresql_serializer
import app.data.sqlite_serializer
import java.io.File

fun main() {

    App.init()

    File("sqlite.plantuml").writeText(sqlite_serializer.plantUML())
    File("postgresql.plantuml").writeText(postgresql_serializer.plantUML())

    val useCases = UseCases()

    println("Welcome to chinook API!")
    println("How can we serve you?")
    println("  1. POST user/signin")
    println("  2. POST user/notification")
    println("  3. POST user/login")

    println("Enter your choice: ")
    val choice = readLine()!!.toInt()

    when (choice) {
        1 -> useCases.signIn.exe(firstName = "firstName", lastName = "lastName")
        2 -> useCases.sendNotifications.exe(message = "message", country = "country")
        3 -> useCases.login.exe(firstName = "firstName", lastName = "lastName")
        else -> throw IllegalArgumentException("Number should be between [1, 3]!")
    }

}
