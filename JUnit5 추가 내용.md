<h1>JUnit 5 추가 내용</h1>

<h2>조건에 따른 테스트</h2>

* JUnit 5는 조건에 따라 테스트를 실행할지 여부를 결정하는 기능을 제공하는데, 이 어노테이션들에 대해 살펴보자.
  * `@EnabledOnOs`, `@DisabledOnOs`
  * `@EnabledOnJre`, `@DisabledOnJre`
  * `@EnabledIfSystemProperty`, `@DisabledIfSystemProperty`
  * `@EnabledIfEnvironmentVariable`, `@DisabledIfEnvironmentVariable`

