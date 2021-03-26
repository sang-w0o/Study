# Django, DjangoRestFramework

- 우선 프로젝트 폴더로 가서 아래 명령어를 입력하여 어드민 페이지를 활성화하자.  
  프로젝트 명은 `MyProject`라 한다.

```
django-admin startproject MyProject
```

- 이후 생성된 `MyProject` 폴더로 가서 아래 명령어를 입력하자.

```
python manage.py migrate
```

- 위 명령어가 생성된 후, 아래 명령어를 통해 서버를 실행해보자.

```
python manage.py runserver
```

- 위에서는 `MyProject`라는 Django Project를 생성했고, 이제 새로운 application을 생성해보자.  
  application명은 `api_basic`라 해보자.

```
python manage.py startapp api_basic
```

- 마지막으로 어드민 페이지에서 사용할 Super user를 아래 명령어를 통해 생성하자.

```
python manage.py createsuperuser
```

<hr/>

# Function based API Views

<h3>서비스 코드 작성하기</h3>

- RESTful API로 JSON 형식의 데이터를 반환하더라도, Django는 view를 사용해야 한다.  
  View는 `views.py`에 저장된다. `api_basic/views.py`에 아래 코드를 추가하자.

```py
from django.shortcuts import render
from django.http import HttpResponse, JsonResponse
from rest_framework.parsers import JSONParser
from .models import Article
from .serializers import ArticleSerializer

def article_list(request):
    if request.method == 'GET':
        articles = Article.objects.all()
        serializer = ArticleSerializer(articles, many=True)
        return JsonResponse(serializer.data, safe=False)

    elif request.method == 'POST':
        data = JSONParser().parse(request)
        serializer = ArticleSerializer(data=data)

        if serializer.is_valid():
            serializer.save()
            return JsonResponse(serializer.data, status=201)
        return JsonResponse(serializer.errors, status=400)
```

- 우선 article_list 메소드는 HTTP Method에 따라 여러 개의 Article들을 불러올지, 저장할지 판단한다.  
  요청 방식은 `request.method`로 판단할 수 있다.

  - `GET` 요청의 경우, 다수의 article객체를 `Article.objects.all()`로 받아와서 그를 토대로 `ArticleSerializer`  
    인스턴스를 생성하고, `JsonResponse()`의 인자로 데이터를 전달하여 응답을 한다.

  - `POST` 요청의 경우, 우선 `JSONParser()`로 Request Body를 파싱한다. 파싱에 성공 했는지의 여부는  
    `Serializer#is_valid()` 메소드를 통해 판단할 수 있다. 파싱에 성공했으면 `serializer.save()`를 호출하여  
    Article객체를 저장하고, 저장된 데이터를 201(CREATED) 상태 코드와 함께 JSON형식으로 반환한다.  
    만약 파싱에 실패한 경우(올바르지 않은 JSON 형식이 제공된 경우)에는 `serializers.errors`를 담은 JSON객체를  
    400(BAD_REQUEST) 상태 코드와 함께 반환한다.

<h3>URL Endpoint 매핑하기</h3>

- 이제 위에서 작성한 서비스 코드와 URL을 매핑할 차례이다.  
  우선 `MyProject/urls.py`에 아래 코드를 추가해주자.

```py
# MyProject/urls.py

from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', include('api_basic.urls'))
]
```

- 위 코드는 일단 아무런 pathPattern이 없는('') 것에 대해서 `api_basic.urls`가 처리할 것임을 의미한다.  
  그러면 이제 `api_basic/` 폴더 하위에 `urls.py`를 작성할 차례이다.

```py
# api_basic/urls.py

from django.urls import path
from .views import article_list

urlpatterns = [
    path('article/', article_list)
]
```

- 이제 `/article`로 `GET` 요청을 보내보면, 아래와 같이 결과가 온다.

```json
[
  {
    "id": 1,
    "title": "sample title",
    "author": "sample author",
    "email": "robby0909@naver.com"
  },
  {
    "id": 2,
    "title": "sample title2",
    "author": "sample author2",
    "email": "sample2@naver.com"
  }
]
```

- 다음으로는 같은 엔드포인트에 `POST` 요청을 보내보자.

* 우선 다양한 경우를 테스트하기 위해 아래와 같이 잘못된 Request Body를 보내보았다.

```json
{
  "hi": "1"
}
```

- 그랬더니 아래와 같이 400(BAD_REQUEST)와 함께 응답이 왔다.

```json
{
  "title": ["This field is required."],
  "author": ["This field is required."],
  "email": ["This field is required."]
}
```

- email 필드에 이메일 형식이 아닌 값을 보내면 아래의 응답이 온다.

```json
{
  "email": ["Enter a valid email address."]
}
```

- 마지막으로 올바른 정보를 보냈더니 아래의 응답이 201(CREATED)와 함께 왔다.

```json
{
  "id": 3,
  "title": "title_Test",
  "author": "author test",
  "email": "test@test.com"
}
```

- 만댝 CSRF Cookie 때문에 로컬에서의 실행이 안된다면, `MyProject/settings.py`의 `MIDDLEWARE` 부분에서  
  `django.middleware.csrf.CsrfViewMiddleware`를 주석처리 하면 된다.

<h3>또다른 API 기능 만들기</h3>

- 이번에는 Article의 ID를 통해 특정 Article의 정보를 가져오거나, 수정하거나 삭제하는 API를 만들어보자.  
  먼저, 서비스 코드부터 작성해보자.

```py
# views.py

# Other method

def article_detail(request, pk):
    try:
        article = Article.objects.get(pk=pk)
    except Article.DoesNotExist:
        return HttpResponse(status=404)

    if request.method == 'GET':
        serializer = ArticleModelSerializer(article)
        return JsonResponse(serializer.data, safe=False)
    elif request.method == 'PUT':
        data = JSONParser().parse(request)
        serializer = ArticleModelSerializer(article, data=data)

        if serializer.is_valid():
            serializer.save()
            return JsonResponse(serializer.data, status=200)
        return JsonResponse(serializer.errors, status=400)
    elif request.method == 'DELETE':
        article.delete()
        return HttpResponse(status=204)
```

- 이제 마찬가지로 위의 서비스 코드가 작동할 엔드포인트를 매핑해보자.  
  위 서비스 코드는 `pk`라는 인자를 받기 때문에 이를 위해 url path pattern을 지정해야 한다.

- 또한 `Article.objects.get(pk=pk)`는 Primary Key를 통해 하나의 객체를 가져오도록 한다.  
  이를 `try-except` 구문으로 묶어주어 `Article.DoesNotExist` 예외를 개치하여 404(NOT_FOUND)를 반환하게 한다.

```py
# api_basic/urls.py

from django.urls import path
from .views import article_list, article_detail

urlpatterns = [
    path('article/', article_list),
    path('detail/<int:pk>', article_detail)
]
```

- 위의 `detail` 다음의 `<int:pk>`가 pk라는 Path Variable이 int형으로 올 것임을 알려준다.

<hr/>

# Function based API에서 api_view() 데코레이터의 사용

- 이전에 `views.py`에서 `article_list()`와 `article_detail()`에서는 직접 `JsonResponse()`를 사용하여  
  JSON형식의 응답을 하며, `JSONParser`를 사용하여 Request Body를 파싱했다. 하지만 rest_framework 라이브러리에서  
  제공하는 클래스들을 사용하면 이를 간단하게 처리할 수 있다.

- 우선 `article_list()` 함수부터 바꿔보도록 하자.

```py
from django.http import HttpResponse
from .models import Article
from .serializers import ArticleModelSerializer
from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status


@api_view(['GET', 'POST'])
def article_list(request):
    if request.method == 'GET':
        articles = Article.objects.all()
        serializer = ArticleModelSerializer(articles, many=True)
        return Response(serializer.data)

    elif request.method == 'POST':
        serializer = ArticleModelSerializer(data=request.data)

        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_201_CREATED)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
```

- `@api_view()`의 인자로 들어간 `GET`, `POST`는 이 메소드는 `GET`, `POST` 요청 이외의 다른  
  HTTP Method 요청은 허가하지 않는다는 것을 알려준다.

- 또다른 차이점으로는 기존에는 `data = JSONParser().parse(request)`를 통해 요청 데이터를  
  파싱했는데, 간단하게 `request.data`로 요청 데이터를 파싱할 수 있다는 것이다.  
  또한 응답 객체도 `JSONResponse`가 아닌 `Response`로 바뀐 것을 확인할 수 있다.

- 마찬가지로 나머지 메소드인 `article_detail()`도 바꿔주도록 하자.

```py
# Other codes..

@api_view(['GET', 'PUT', 'DELETE'])
def article_detail(request, pk):
    try:
        article = Article.objects.get(pk=pk)
    except Article.DoesNotExist:
        return HttpResponse(status=status.HTTP_404_NOT_FOUND)

    if request.method == 'GET':
        serializer = ArticleModelSerializer(article)
        return Response(serializer.data)
    elif request.method == 'PUT':
        serializer = ArticleModelSerializer(article, data=request.data)

        if serializer.is_valid():
            serializer.save()
            return Response(serializer.data, status=status.HTTP_200_OK)
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    elif request.method == 'DELETE':
        article.delete()
        return HttpResponse(status=status.HTTP_204_NO_CONTENT)
```

<hr/>
