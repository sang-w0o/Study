# Kotlin Coroutines

- Kotlin은 하나의 언어로서 다른 라이브러리들이 Coroutine을 활용하게 하기 위해  
  최소한의 low-level API를 제공한다. JavaScript의 `async`, `await` 문법과 달리  
  Kotlin에서는 이러한 키워드들을 제공하지 않는다. Kotlin의 _Suspending Function_ 개념은  
  비동기 작업을 수행할 때 더욱 안전하고 에러에 대처하기 쉽도록 해준다.

- JetBrains 사에서는 `kotlinx.coroutines`라는 라이브러리를 제공한다.  
  이 라이브러리는 여러 가지 coroutine을 지원하는 기능들을 제공해준다.
