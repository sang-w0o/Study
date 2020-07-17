Mustache로 화면 구성
======

<h2>서버 템플릿 엔진과 Mustache 소개</h2>

* Template Engine : __지정된 템플릿 양식과 데이터가 합쳐져 HTML 문서를 출력하는 소프트웨어__   
  Spring, Servlet 사용시에는 JSP, Freemarker 등이 있고, React, Vue, Angular등의 View파일들도 해당된다.

* 하지만 JSP, Freemarker 는 __서버 템플릿 엔진__ 이라 불리며, React, Vue 등은 __클라이언트 템플릿 엔진__ 이라 불린다.
* 예를 들어 아래 코드를 보자.
```jsp
<script type="text/javascript">
$(document).ready(function() {
    if(a=="1") {
        <%
            System.out.println("test");
        %>
    }
});
```
* 위 코드는 if문에 관계 없이 무조건 콘솔에 test를 출력하는데, 이는 __js와 JSP가 작동하는 영역이 다르기 때문__ 이다.   
  JSP를 비롯한 서버 템플릿 엔진은 __서버에서 구동__ 된다.
* 서버 템플릿 엔진을 이용한 화면 생성은 __서버에서 Java 코드로 문자열__ 을 만든 뒤, 이 문자열을 HTML로 변환하여 __브라우저로 전달__ 하게 된다.
* 반면에 Js는 __브라우저 상에서 작동__ 한다. 위에서 작성된 js 코드가 실행되는 장소는 서버가 아닌 __브라우저__ 이다.   
  즉, 브라우저에서 작동될 때는 서버 템플릿 엔진의 범위를 벗어나기 때문에 제어할 수가 없다.
* Vue.js, React.js를 이용한 SPA(Single Page Application)은 __브라우저에서 화면을 생성__ 한다.   
  즉, __서버에서 이미 코드가 벗어난 경우__ 이다. 그래서 서버에서는 JSON, XML 형식의 데이터만 전달하고   
  클라이언트에서 이 데이터를 조립하게 된다. 최근에는 React, Vuew 등 Js Framework에서 Server-side Rendering을 지원하는데, 이는   
  JavaScript Framework의 화면 생성 방식을 서버에서 실행하는 것을 의미한다. 이는 V8 엔진 라이브러리를 지원하기 때문이며,   
  Spring-boot에서 사용할 수 있는 대표적인 기술로는 Nashorn, J2V8이 있다.
<hr/>

<h3>Mustache란</h3>

* Mustache는 수많은 언어를 지원하는 가장 간단한 템플릿 엔진이다. 즉, JSP와 같이 HTML을 만들어주는 템플릿 엔진 중 하나이다.
* Mustache는 Ruby, Js, Python, PHP, Java 등 현존하는 대부분의 언어를 지원하고 있다.   
  따라서 Java에서 사용될 때는 서버 템플릿 엔진으로, Js에서 사용될 때는 클라이언트 템플릿 엔진으로 모두 사용할 수 있다.

* Mustache의 장점은 아래와 같다.
  * 문법이 다른 템플릿 엔진보다 간단하다.
  * logic code를 사용할 수 없어 View의 역할과 서버의 역할이 명확하게 분리된다.
  * Mustache.js, Mustache.java가 모두 있어 하나의 문법으로 클라이언트와 서버 템플릿을 모두 사용할 수 있다.
* __템플릿 엔진은 화면 구성 역할에만 충실해야 한다__.
<hr/>

<h3>Mustache Plugin의 설치</h3>

* Intellij에서 Mustache는 Marketplace에서 플러그인을 검색해서 설치하면 된다.
<hr/>

<h2>기본 페이지 만들기</h2>

* Spring boot에서 Mustache를 사용하기 위해서는 `build.gradle`에 다음 의존성을 추가해야 한다.
```js
dependencies {
    compile('org.springframework.boot:spring-boot-starter-mustache')
}
```
* 위 코드처럼 Mustche는 Spring Boot에서 공식 지원하는 템플릿 엔진이다.
* Muatache 파일 위치는 기본적으로 `src/main/resources/templates` 이다.   
  이 위치에 Muatache 파일을 두면, Spring Boot에서 자동으로 로딩하게 된다.

* 첫 페이지를 담당할 `index.mustache`를 `src/main/resources/templates` 하위에 생성하자.
```html
<!DOCTYPE HTML>
<html>
<head>
    <title>Spring-boot web service.</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
</head>
<body>
    <h1>Web Service using Spring-Boot.</h1>
</body>
</html>
```
* 다음으로는 이 Mustache에 URL을 매핑하자. URL매핑은 당연히 컨틀롤러에서 진행하며, web 패키지 내에 `IndexController`를 추가하자.
```java
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    
    @GetMapping("/")
    public String index() {
        return "index";
    }
}
```
* Mustache Starter 덕분에 컨트롤러에서 문자열을 반환할 때 __앞의 경로와 뒤의 확장자는 자동으로 저장__ 된다.   
  위 매핑에서 앞의 경로는 `src/main/resources/templates`이며, 뒤의 확장자는 `.mustache`가 붙는다.   
  위에서는 "index"를 반환하므로 `src/main/resources/templates/index.mustache`로 전환되어 `View Resolver`가 처리하게 된다.

* 다음으로는 위 클래스에 대한 테스트 코드인 `IndexControllerTest` 클래스를 작성하자.
```java
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IndexControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void load_main_page() throws Exception {
        //when
        String body = this.restTemplate.getForObject("/", String.class);
        
        //then
        assertThat(body).contains("Web Service using Spring-Boot.");
    }
}
```
* 위 테스트 코드는 실제로 URL 호출 시, 페이지의 내용이 제대로 호출되는지에 대한 테스트이다.
* HTML도 __규칙이 있는 문자열__ 이기에, `TestRestTemplate`객체를 통해 HTTP GET Method로 "/"로 호출했을 때   
  `index.mustache`에 포함된 코드들이 확인하면 된다.
<hr/>

<h2>게시글 등록 화면의 생성</h2>

* BootStrap, jQuery 등 Frontend Library를 사용하는 방법은 크게 두 가지가 있는데, 하나는 __외부 CDN을 사용하는 것__ 이고,   
  다른 하나는 __직접 라이브러리를 받아서 사용하는 것__ 이다.
* 라이브러리를 받아서 사용하는 법은 아니, 외부 CDN을 사용하는 법을 알아보자.
* Bootstrap, jQuery를 `index.mustache`에 추가해야 하는데, __레이아웃 방식__ 으로 추가해보자.   
  __Layout 방식__ 은 __공통 영역을 별도의 파일로 분리하여 필요한 곳에서 가져다 쓰는 방식__ 을 의미한다.   
  Boostrap, jQuery는 __mustache 화면 어디서나 필요__ 하기에, 해당 라이브러리를 가져오는 코드를 반복하는 것을 하지 않기 위해   
  `src/main/resources/templates`의 하위에 `layout` 디렉토리를 생성하고, 여기에 `footer.mustache`와 `header.mustache`를 추가한다.
* 아래는 `header.mustache` 의 코드이다.
```html
<!DOCTYPE HTML>
<html>
<head>
    <title>Spring-Boot WebService.</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

    <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css">
</head>
<body>
```
* 아래는 `footer.mustache`의 코드이다.
```html
<script src="https://code.jquery.com/jquery-3.3.1.min.js"></script>
<script src="https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/js/bootstrap.min.js"></script>

<script src="/js/app/index.js"></script>
</body>
</html>
```
* 위 코드를 보면 css와 js의 위치가 다른데, 이는 __페이지 로딩 속도를 높이기 위함__ 이다.   
  css는 header에, js는 footer에 위치했다. 이는 HTML은 위에서부터 코드가 실행되기 때문에 __head가 다 실행되어야 body가 실행__ 되기 때문이다.   
  즉 head가 다 불러지지 않으면 사용자 쪽에선 백지 화면만 출력된다. 특히 js의 용량이 크면 클수록 body 부분의 실행이 늦어지기 때문에   
  js는 body태그의 하단에 두어 화면이 다 그려진 후에 호출하는 것이 좋다. 반면에 css는 화면을 그리는 역할이므로 head에서 불러오는 것이   
  좋다. 그렇지 않으면 css가 적용되지 않은 깨진 화면을 사용자가 볼 수 있기 때문이다.   
  추가적으로 `bootstrap.js`의 경우, __jQuery가 꼭 있어야만__ 하기 때문에 jQuery를 bootstrap보다 먼저 불러오게 작성했다.   
  이런 상황을 `bootstrap.js`가 `jQuery`에 의존한다 표현한다.

* 라이브러리를 비롯해 기타 HTML 태그들이 모두 레이아웃에 추가되니 이제 `index.mustache`에는 필요한 코드만 남게 된다.   
  `index.mustache`의 코드는 아래와 같이 변경된다.
```m
{{>layout/header}}

<h1>Web Service using Spring-Boot.</h1>

{{>layout/footer}}
```
* `{{>layout/header}}`에서 `{{>}}`는 현재 mustache 파일 기준으로 다른 파일을 가져온다.   
  위에서 `index.mustache`는 `src/main/resources/templates`에 위치하고, header와 footer 파일들은 `src/main/resources/templates/layout`   
  에 위치하기 때문에 위처럼 지정한 것이다.
* 레이아웃으로 파일을 분류했으니 `index.mustache`에 글 등록 버튼을 하나 추가해보자.
```m
{{>layout/header}}

<h1>Web Service using Spring-Boot.</h1>
<div class="col-md-12">
    <div class="row">
        <div class="col-md-6">
            <a href="/posts/save" role="button" class="btn btn-primary">글 등록</a>
        </div>
    </div>
</div>

{{>layout/footer}}
```
* 위에서는 `<a>` 태그를 이용해 글 등록 페이지로 이동하는 글 등록 버튼을 생성했다.   
  이동할 페이지의 주소는 `/posts/save` 이다. 이제 이 주소에 해당하는 컨트롤러를 생성하자.   
  페이지에 관련된 컨트롤러는 모두 `IndexController`를 사용한다.
```java
// IndexController.java

@GetMapping("/posts/save")
public String postsSave() {
    return "posts-save";
}
```
* 위 코드에서는 `/posts/save`로 요청이 오면 "posts-save.mustache"를 호출하도록 한다.   
  따라서 이에 매칭되는 `posts-save.mustache`파일을 생성하자. 파일 위치는 `index.mustache`와 동일하다.
```m
{{>layout/header}}

<h1>게시글 등록</h1>

<div class="col-md-12">
    <div class="col-md-4">
        <form>
            <div class="form-group">
                <label for="title">제목</label>
                <input type="text" class="form-control" id="title" placeholder="제목 입력">
            </div>
            <div class="form-group">
                <label for="author">작성자</label>
                <input type="text" class="form-control" id="author" placeholder="작성자 입력">
            </div>
            <div class="form-group">
                <label for="content">내용</label>
                <textarea class="form-control" id="content" placeholder="내용 입력"></textarea>
            </div>
        </form>
        <a href="/" role="button" class="btn btn-secondary">취소</a>
        <button type="button" class="btn btn-primary" id="btn-save">등록</button>
    </div>
</div>

{{>layout/footer}}
```
* 이제 "등록" 버튼의 동작을 정의할 js 파일을 작성하자.   
  `src/main/resources/` 하위에 `static/js/app` 폴더를 생성하고, 거기에 `index.js` 파일을 생성하자.
* 아래는 `index.js`의 코드이다.
```js
var main={
    init : function() {
        var _this = this;
        $('#btn-save').on('click', function() {
            _this.save();
        })
    },
    save : function() {
        var data = {
            title : $('#title').val(),
            author: $('#author').val(),
            content : $('#content').val()
        };
        
        $.ajax({
            type:'POST',
            url:'/api/v1/posts',
            contentType:'application/json; charset=utf-8',
            data:JSON.stringify(data)
        }).done(function() {
            alert('글이 등록되었습니다.');
            window.location.href="/";
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    }
};

main.init();
```

* 이전에 `footer.mustache`파일에는 아래 다음 script 태그가 있었다.
```html
<script src="/js/app/index.js"></script>
```
* 여기서 호출 경로는 절대경로(/)로 바로 시작하는데, Spring Boot는 기본적으로 `src/main/resources/static`에 위치한 Js, CSS, 이미지   
  등의 정적 파일들은 URL에서 /로 설정한다. 그래서 다음과 같이 파일이 위치하면 위치에 맞게 호출이 가능하다.
  * `src/main/resources/static/js/ ==> http://domain/js/`
  * `src/main/resources/static/css/ ==> http://domain/css/`  
<hr/>

<h2>전체 조회 화면 만들기</h2>

* 전체 조회를 위해 `index.mustache`의 UI를 아래와 같이 변경하자.
```m
{{>layout/header}}

<h1>Web Service using Spring-Boot.</h1>
<div class="col-md-12">
    <div class="row">
        <div class="col-md-6">
            <a href="/posts/save" role="button" class="btn btn-primary">글 등록</a>
        </div>
    </div>
    <br/>
    
    <!-- 목록 출력 영역 -->
    <table class="table table-horizontal table-bordered">
        <thead class="thead-strong">
            <tr>
                <th>게시글 번호</th>
                <th>제목</th>
                <th>작성자</th>
                <th>최종 수정일</th>
            </tr>
        </thead>
        <tbody id="tbody">
            {{#posts}}
                <tr>
                    <td>{{id}}</td>
                    <td>{{title}}</td>
                    <td>{{author}}</td>
                    <td>{{modifiedDate}}</td>
                </tr>
            {{/posts}}
        </tbody>
    </table>
</div>

{{>layout/footer}}
```
* 위에서는 Mustache의 문법이 처음 사용됐는데, 설명은 아래와 같다.
  * `{{#posts}}` : posts라는 List를 순회한다. Java의 for문과 동일하다.
  * `{{id}}` 등의 변수명 : List에서 뽑아낸 객체의 필드를 사용한다.

* 이제 Controller, Service, Repository 코드를 작성해보자.   
  먼저 Repository부터 시작하자. 기존의 `PostsRepository` 인터페이스에 쿼리를 추가한다.
```java
public interface PostsRepository extends JpaRepository<Posts, Long> {
    
    @Query("SELECT p FROM Posts p ORDER BY p.id DESC")
    List<Posts> findAllDesc();

}
```
* `Spring Data JPA`에서 제공하지 않는 메소드는 위와 같이 __@Query__ 어노테이션의 값에 쿼리문을 작성하여 메소드를 만들 수 있다.

* 다음으로는 `PostsService`에 조회를 위한 코드를 추가해보자.
```java
// PostsService.java
@Transactional(readOnly = true)
public List<PostsListResponseDto> findAllDesc() {
    return postsRepository.findAllDesc().stream().map(PostsListResponseDto::new).collect(Collectors.toList());
}
```
* 위 코드에서는 `findAllDesc()` 메소드의 어노테이션 __@Transactional__ 에 readOnly 속성이 true로 추가되었다.   
  이 옵션은 __transaction범위는 유지하되, 조회 기능만 남겨두어 조회 속도를 개선__ 한다.   
  등록, 수정, 삭제 기능이 전혀 없는 서비스 메소드에서 사용하는 것이 좋다.

* 다음으로는 응답으로 보낼 `PostsListResponseDto` 클래스를 작성하자.
```java
@Getter
public class PostsListResponseDto {
    
    private Long id;
    private String title;
    private String author;
    private LocalDateTime modifiedDate;
    
    public PostsListResponseDto(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.modifiedDate = entity.getModifiedDate();
    }
}
```
* 마지막으로 Controller를 변경하자.
```java
@RequiredArgsConstructor
@Controller
public class IndexController {
    
    private final PostsService postsService;
    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("posts", postsService.findAllDesc());
        return "index";
    }

    @GetMapping("/posts/save")
    public String postsSave() {
        return "posts-save";
    }
}
```
* 위 코드의 `index()` 메소드로 전달된 `Model`객체는 서버 템플릿 엔진에서 사용할 수 있는 객체를 저장할 수 있다.   
  위에서는 `postsService.findAllDesc()`로 가져온 결과를 posts라는 객체로 `index.mustache`에 전달한다.
<hr/>

<h2>게시글 수정, 삭제 화면 만들기</h2>

* 앞에서 작성한 `PostsApiController`의 아래 메소드로 요청하는 화면을 만들어보자.
```java
@PutMapping("/api/v1/posts/{id}")
public Long update(@PathVariable Long id, @RequestBody PostsUpdateRequestDto requestDto) {
    return postsService.update(id, requestDto);
}
```

<hr/>

<h3>게시글 수정</h3>

* `posts-update.mustache`를 `src/main/resoucres/templates` 하위에 아래와 같이 작성하자.
```m
{{>layout/header}}

<h1>게시글 수정</h1>

<div class="col-md-12">
    <div class="col-md-4">
        <form>
            <div class="form-group">
                <label for="id">글 번호</label>
                <input type="text" class="form-control" id="id" value="{{post.id}}" readonly>
            </div>
            <div class="form-group">
                <label for="title">제목</label>
                <input type="text" class="form-control" id="title" value="{{post.title}}">
            </div>
            <div class="form-group">
                <label for="author">작성자</label>
                <input type="text" class="form-control" id="author" value="{{post.author}}" readonly>
            </div>
            <div class="form-group">
                <label for="content">내용</label>
                <textarea class="form-control" id="content">{{post.content}}</textarea>
            </div>
        </form>
        <a href="/" role="button" class="btn btn-secondary">취소하기</a>
        <button type="button" class="btn btn-primary" id="btn-update">수정 완료</button>
    </div>
</div>

{{>layout/footer}}
```
* `{{post.id}}` : Mustache는 객체의 필드 접근 시 `.`을 이용한다. 즉, 이 코드는 Post클래스의 id에 접근한 것이다.
* `readonly` : `<input>` 태그에 읽기 권한만 허용하는 속성이다.
* 또한 `btn-update` 버튼을 클릭하면 update기능을 호출할 수 있게끔 `index.js`에 함수를 하나 추가하자.
```js
var main={
    init : function() {
        var _this = this;
        $('#btn-save').on('click', function() {
            _this.save();
        })
        
        $('#btn-update').on('click', function() {
            _this.update();
        })
    },
    save : function() {
        var data = {
            title : $('#title').val(),
            author: $('#author').val(),
            content : $('#content').val()
        };

        $.ajax({
            type:'POST',
            url:'/api/v1/posts',
            contentType:'application/json; charset=utf-8',
            data:JSON.stringify(data)
        }).done(function() {
            alert('글이 등록되었습니다.');
            window.location.href="/";
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },
    update:function() {
        var data = {
            title : $('#title').val(),
            content : $('#content').val()
        };
        
        var id = $('#id').val();
        
        $.ajax({
            type:'PUT',
            url:'/api/v1/posts/' + id,
            dataType:'json',
            contentType: 'application/json; charset=utf-8',
            data:JSON.stringify(data)
        }).done(function() {
            alert('글이 수정되었습니다.');
            window.location.href = "/";
        }).fail(function(e) {
            alert(JSON.stringify(e));
        })
    }
};

main.init();
```
* 이제 수정 화면으로 연결할 Controller 코드를 작업하자. `IndexController`에 다음 메소드를 추가한다.
```java
@GetMapping("/posts/update/{id}")
public String postsUpdate(@PathVariable Long id, Model model) {
    PostsResponseDto dto = postsService.findById(id);
    model.addAttribute("post", dto);
    return "posts-update";
}
```
<hr/>

<h3>게시글 삭제</h3>

* 게시글 삭제 기능도 본문을 확인하고 삭제하게 하기 위해, 수정 화면인 `posts-update.mustache`에 버튼을 하나 추가하자.
```m
<button type="button" class="btn btn-danger" id="btn-delete">삭제</button>
```
* 다음으로는 삭제 이벤트를 진행할 JS 코드를 추가하자.
```js
// index.js
var main={
    init : function() {
        var _this = this;
        $('#btn-save').on('click', function() {
            _this.save();
        });

        $('#btn-update').on('click', function() {
            _this.update();
        });
        
        $('#btn-delete').on('click', function() {
            _this.delete();
        })
    },
    save : function() {
        var data = {
            title : $('#title').val(),
            author: $('#author').val(),
            content : $('#content').val()
        };

        $.ajax({
            type:'POST',
            url:'/api/v1/posts',
            contentType:'application/json; charset=utf-8',
            data:JSON.stringify(data)
        }).done(function() {
            alert('글이 등록되었습니다.');
            window.location.href="/";
        }).fail(function(error) {
            alert(JSON.stringify(error));
        });
    },
    update:function() {
        var data = {
            title : $('#title').val(),
            content : $('#content').val()
        };

        var id = $('#id').val();

        $.ajax({
            type:'PUT',
            url:'/api/v1/posts/' + id,
            dataType:'json',
            contentType: 'application/json; charset=utf-8',
            data:JSON.stringify(data)
        }).done(function() {
            alert('글이 수정되었습니다.');
            window.location.href = "/";
        }).fail(function(e) {
            alert(JSON.stringify(e));
        })
    },
    delete:function() {
        var id = $('#id').val();
        $.ajax({
            type:'DELETE',
            url:'/api/v1/posts/' + id,
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
        }).done(function() {
            alert('글이 삭제되었습니다.');
            window.location.href = '/';
        }).fail(function(e) {
            alert(JSON.stringify(e));
        })
    }
};

main.init();
```

* 위에서 수정 및 조회 기능과 마찬가지로 삭제 API를 만들어보자. 먼저 서비스 메소드를 추가한다.
```java
// PostsService.java

@Transactional
public void delete(Long id) {
    Posts posts = postsRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
    postsRepository.delete(posts);
}
```
* `postsRepository.delete()` 메소드는 이미 `JpaRepository`에서 제공하고 있는 메소드이다.   
  Entity를 파라미터로 삭제할 수도 있고, `deleteById()` 메소드를 사용하면 id로 삭제할 수 있다.

* 다음으로는 서비스에서 만든 delete 메소드를 컨트롤러가 사용하도록 코드를 추가하자.
```java
@DeleteMapping("/api/v1/posts/{id}")
public Long delete(@PathVariable Long id) {
    postsService.delete(id);
    return id;
}
```