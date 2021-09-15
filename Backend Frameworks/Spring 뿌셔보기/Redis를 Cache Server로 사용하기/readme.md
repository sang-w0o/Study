# Redis를 Cache Server로 사용하기

- Cache가 없다면, 자주 조회되는 리소스를 가져오기 위해 요청일 들어올 때마다 DB에 접근해서  
  원하는 정보를 가져와야 한다. 이는 굉장히 비효율적이므로, Cache를 적용해보자.

- 이때, Cache 정보를 담기 위한 Database는 가볍고, 인 메로리로 작동하며  
 성능이 매우 뛰어는 Redis를 사용할 것이다.
<hr/>

- 모든 소스 코드는 <a href="https://github.com/Example-Collection/Spring-Redis-Cache-Example">링크</a>에서 확인할 수 있다.
