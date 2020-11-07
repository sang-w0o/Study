
import org.junit.Assert.assertNotEquals
import org.junit.jupiter.api.Test

class BaseballGameTest {

    var baseballGame = BaseballGame()

    @Test
    fun makeRandomNumbers() {
        val computerNumber = baseballGame.getComputerNumber()
        assertNotEquals(computerNumber[0], computerNumber[1])
        assertNotEquals(computerNumber[0], computerNumber[2])
        assertNotEquals(computerNumber[1], computerNumber[2])
    }
}