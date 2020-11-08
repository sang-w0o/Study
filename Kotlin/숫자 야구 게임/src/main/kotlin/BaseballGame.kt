import java.util.*

class BaseballGame() {
    private var computerNumber: HashSet<Int> = HashSet<Int>(3)
    private var userNumber: ArrayList<Int>

    init {
        computerNumber = createRandomNumbers()
        userNumber = ArrayList(3)
    }

    fun getComputerNumber() = computerNumber

    private fun createRandomNumbers(): HashSet<Int> {
        while (computerNumber.size != 3) {
            computerNumber.add(Random().nextInt(9) + 1)
        }
        return computerNumber
    }

    fun inputNumbers() {
        print("숫자를 입력해주세요: ")
        val userInputString = readLine()
        userNumber.clear()
        if (userInputString != null && userInputString.length == 3) {
            for (i in userInputString.indices) {
                userNumber.add(Integer.parseInt(userInputString[i].toString()))
            }
        }
    }

    private fun getStrikeCounts(): Int {
        var result = 0
        for (i in 0..2) {
            if (computerNumber.toArray()[i] == userNumber.toArray()[i]) {
                ++result
            }
        }
        return result
    }

    private fun getBallCounts(): Int {
        var result = 0
        for (i in 0..2) {
            for (j in 0..2) {
                if ((i != j) && (userNumber.toArray()[i] == computerNumber.toArray()[j])) {
                    ++result
                }
            }
        }
        return result
    }

    private fun printScore(strikeCounts: Int, ballCounts: Int) {
        if ((strikeCounts != 0) && (ballCounts != 0)) {
            println("$strikeCounts 스트라이크 $ballCounts 볼")
        } else if (strikeCounts == 0 && ballCounts > 0) {
            println("$ballCounts 볼")
        } else if (ballCounts == 0 && strikeCounts > 0) {
            println("$strikeCounts 스트라이크")
        } else {
            println("4볼")
        }
    }

    fun validateScore(): Boolean {
        val strikeCounts = getStrikeCounts()
        val ballCounts = getBallCounts()
        val result = strikeCounts == 3
        printScore(strikeCounts, ballCounts)
        return result
    }


    companion object {
        fun play() {
            var baseballGame = BaseballGame()
            while (true) {
                baseballGame.inputNumbers()
                if (baseballGame.validateScore()) {
                    println("3개의 숫자를 모두 맞히셨습니다! 게임종료\n 게임을 새로 시작하면 1, 종료하려면 2를 입력하세요.")
                    val menu = Integer.parseInt(readLine())
                    if (menu == 1) baseballGame = BaseballGame()
                    else break
                }
            }
        }
    }
}