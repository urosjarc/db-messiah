package core.use_cases

import app.repos.ArtistRepo
import app.repos.CustomerRepo
import app.services.EmailService
import core.domain.Id

class Login(
    val emailService: EmailService,
    val customerRepo: CustomerRepo,
    val artistRepo: ArtistRepo
) {

    fun exe(firstName: String, lastName: String) {

        val customer = this.customerRepo.getCustomer(pk = Id(23))
        val artist = this.artistRepo.getArtist(pk = Id(66))

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
