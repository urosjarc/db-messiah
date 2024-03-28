package domain

import Id
import java.time.LocalDate

data class Employee(
    val id: Id<Employee> = Id(),
    val firstName: String,
    val lastName: String,
    val title: String,
    val reportsTo: Id<Employee>?,
    val birthDate: LocalDate,
    val hireDate: LocalDate,
//    val company: String,
//    val address: String,
//    val city: String,
//    val state: String,
//    val country: String,
//    val postalCode: Int,
//    val phone: String,
//    val fax: String,
//    val email: String
)
