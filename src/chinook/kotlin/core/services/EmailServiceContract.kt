package core.services

interface EmailServiceContract {
    fun sendEmail(emails: List<String>, content: String): Boolean
}
