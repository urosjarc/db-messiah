package core.use_cases

import app.repos.ArtistRepo
import app.repos.CustomerRepo
import app.services.EmailService

class SignIn(
    val emailService: EmailService,
    val customerRepo: CustomerRepo,
    val artistRepo: ArtistRepo
) {

    fun exe(firstName: String, lastName: String) {

        val isSent = emailService.sendEmail(
            emails = listOf("$firstName.$lastName@gmail.com"), content = """
            Welcome please sign in here <link>
            In case of problems please give us a call! 
            Good day sir!
        """.trimIndent()
        )

        //Do something more

    }
}
