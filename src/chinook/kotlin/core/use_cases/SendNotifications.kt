package core.use_cases

import app.repos.CustomerRepo
import app.services.EmailService
import core.domain.Id

class SendNotifications(
    val emailService: EmailService,
    val customerRepo: CustomerRepo
) {
    fun exe(message: String, country: String){

        val customer = this.customerRepo.getCustomer(pk = Id(23))

        val isSent = this.emailService.sendEmail(
            emails = listOf("${customer.firstName}.${customer.lastName}@gmail.com"), content = """
            Welcome we want to notify you <link>
            In case of problems please give us a call! 
            Good day sir!
        """.trimIndent()
        )

        //Do something more
    }
}
