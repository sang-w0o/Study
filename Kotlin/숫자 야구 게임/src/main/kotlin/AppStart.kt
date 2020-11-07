import java.lang.IllegalArgumentException

class AppStart {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val baseballGame = BaseballGame()
            try {
                baseballGame.inputNumbers()
            } catch(exception: IllegalArgumentException) {
                println("숫자를 잘못 입력하셨습니다.")
            }
        }
    }
}