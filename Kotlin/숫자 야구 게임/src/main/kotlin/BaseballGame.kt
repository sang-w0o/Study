import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList

class BaseballGame() {
    private var computerNumber: ArrayList<Int> = ArrayList(3)
    private var userNumber: ArrayList<Int>

    init {
        computerNumber = createRandomNumbers()
        userNumber = ArrayList(3)
    }

    fun getComputerNumber() = computerNumber

    private fun createRandomNumbers() : ArrayList<Int> {
        for (i in 0..2) {
            if(i == 0) {
                computerNumber.add(Random().nextInt(9) + 1)
            }
            if(i >= 1) {
                do {
                    val tmp = Random().nextInt(9) + 1
                    computerNumber.add(tmp)
                } while (tmp != computerNumber[i])
            }
        }
        return computerNumber
    }

    fun inputNumbers() {
        print("숫자를 입력해주세요: ")
        val userInputString = readLine()
        if (userInputString != null && userInputString.length == 3) {
            for(i in userInputString.indices) {
                userNumber.add(Integer.parseInt(userInputString[i].toString()))
            }
        } else {
            throw IllegalArgumentException()
        }
    }

    fun validateScore() {

    }
}