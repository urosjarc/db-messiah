package app.services

import core.services.EmailServiceContract

class EmailService(
    val token: String
): EmailServiceContract {
    override fun sendEmail(emails: List<String>, content: String): Boolean {
        //Send email here :)
        return true
    }
}
