import core.use_cases.Login
import core.use_cases.SendNotifications
import core.use_cases.SignIn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UseCases : KoinComponent {
    val signIn: SignIn by this.inject()
    val login: Login by this.inject()
    val sendNotifications: SendNotifications by this.inject()
}
