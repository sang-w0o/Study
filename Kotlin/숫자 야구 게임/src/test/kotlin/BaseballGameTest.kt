import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class BaseballGameTest {

    var baseballGame = BaseballGame()

    @Test
    fun makeRandomNumbers() {
        val computerNumber = baseballGame.getComputerNumber()
        assertNotEquals(computerNumber.toArray()[0], computerNumber.toArray()[1])
        assertNotEquals(computerNumber.toArray()[0], computerNumber.toArray()[2])
        assertNotEquals(computerNumber.toArray()[1], computerNumber.toArray()[2])
    }
}