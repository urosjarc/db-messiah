import app.App
import core.use_cases.Login
import core.use_cases.SendNotifications
import core.use_cases.SignIn
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.koin.core.component.inject
import org.koin.test.KoinTest

class Test_Chinook : KoinTest {

    val signIn: SignIn by this.inject()
    val login: Login by this.inject()
    val sendNotifications: SendNotifications by this.inject()

    companion object {
        @JvmStatic
        @BeforeAll
        fun init() {
            App.init()
        }
    }

    @Test
    fun `test signIn`(){
        signIn.exe(firstName = "firstName", lastName = "lastName")
    }
    @Test
    fun `test sendNotifications`(){
        sendNotifications.exe(message = "message", country = "country")
    }
    @Test
    fun `test login`(){
        login.exe(firstName = "firstName", lastName = "lastName")
    }
}
