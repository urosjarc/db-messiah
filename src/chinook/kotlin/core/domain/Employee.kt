package core.domain

import kotlinx.datetime.LocalDate

data class Employee(
    val employeeId: Id<Employee>? = null,
    val firstName: String,
    val lastName: String,
    val title: String,
    val reportsTo: Id<Employee>?,
    val birthDate: LocalDate,
    val hireDate: LocalDate,
    val company: String,
    val address: String,
    val city: String,
    val state: String,
    val country: String,
    val postalCode: Int,
    val phone: String,
    val fax: String,
    val email: String
)
