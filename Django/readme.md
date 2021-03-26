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

# Serializer

<h3>Article Model 생성</h3>

- 우선 위에서 `api_basic`이라는 애플리케이션을 생성하면 `api_basic`폴더 하위에  
  `models.py`라는 파일이 생성된다.

- `Serializer`는 요청에 대한 응답을 JSON 형식으로 변환해주는 기능을 제공하는 일종의 변환기이다.  
  우선, 예시로 `Article`라는 테이블이 있다고 하고, 이를 파이썬 코드로 표현해보자.

```py
# models.py

class Article(models.Model):
    title = models.CharField(max_length=100)
    author = models.CharField(max_length=100)
    email = models.EmailField(max_length=100)

    def __str__(self):
        return self.title
```

- 위에서 작성하는 `__str__` 함수는 어드민 페이지에서 테이블의 데이터로 출력되는 값을 의미한다.

* 다음으로, `MyProject` 폴더 하위에 있는 `settings.py`의 `INTALLED_APPS`에 아래 2개를 추가해주자.

```py
# settings.py

# 다른 설정들
# Application definition

INSTALLED_APPS = [
    # 기존의 값들
    'rest_framework',
    'api_basic'
]
```

- 이제 이렇게 생성된 모델이 실제 테이블에 들어가게 하기 위해 migration을 진행해야 한다.  
  `MyProject` 폴더로 이동 후 아래 명령어를 입력하자.

```
python manage.py makemigrations

python manage.py migrtate
```

- 마지막으로 어드민 페이지에서 Article에 대한 정보를 볼 수 있도록 `admin.py`에 아래를 추가해주자.

```py
# admin.py

from django.contrib import admin
from .models import Article

admin.site.register(Article)
```

- 이제 어드민 페이지 (`http://localhost:8000/admin`)에 가면 Article을 볼 수 있다.

<h3>Article Serializer 작성</h3>

- Article을 JSON으로 변환해줄 Serializer를 작성해보자.  
  이 파일은 `api_basic`폴더 하위에 `serializers.py`로 하자.

```py
from rest_framework import serializers
from .models import Article

class ArticleSerializer(serializers.Serializer):
    title = serializers.CharField(max_length=100)
    author = serializers.CharField(max_length=100)
    email = serializers.EmailField(max_length=100)

    def create(self, validated_data):
        return Article.objects.create(validated_data)

    def update(self, instance, validated_data):
        instance.title = validated_data.get('title', instance.title)
        instance.author = validated_data.get('author', instance.author)
        instance.email = validated_data.get('email', instance.email)
        instance.save() # 인스턴스를 save한다.
        return instance

```

- `serializers.Serializer`를 상속하는 클래스를 만들 때에는 JSON에 포함될 필드들을 선언해야 한다.  
  위에서는 title, author, email의 필드를 선언해주었다.
- `create()`와 `update()` 메소드는 우리가 원하는 필드들(title, author, email)이 포함된  
  데이터(validated_data)가 주어질 때 인스턴스를 저장하거나 UPDATE하는 메소드이다.

<h3>Testing ArticleSerializer</h3>

- 이제 위에서 작성한 `ArticleSerializer`를 테스트 해보자.  
  `python manage.py shell`로 파이썬 쉘에 들어온 후, 아래 코드를 입력 해보자.

```py
# Testing ArticleSerializer

from api_basic.models import Article
from api_basic.serializers import ArticleSerializer
from rest_framework.renderers import JSONRenderer
from rest_framework.parsers import JSONParser

# Article 객세 생성
article = Article(title = 'sample title', author = 'sample author', email = 'sample@sample.com')

# Article 객체를 DB에 저장
article.save()

# ArticleSerializer 객체 생성
serializer = new ArticleSerializer(article)

# JSON 형식으로 article 데이터 보기
serializer.data
# 위 코드의 결과 : {'title': 'sample title', 'author': 'sample author', email: 'sample@email.com'}

# 응답(Response)에 담을 JSON Response Body 생성
content = JSONRenderer().render(serializer.data)
```

<hr/>

# ModelSerializer

- 위에서 작성한 `ArticleSerializer`는 title, author, email 등 `Article`의 정보를 명시해야 한다.  
  코드의 중복을 방지하기 위해 `Serializer`가 아니라 `ModelSerializer`를 상속하도록 해보자.

```py
# serializers.py

class ArticleSerializer(serializers.ModelSerializer):
    class Meta:
        model = Article
        fields = ['id', 'title', 'author', 'email']
```

- 수정된 `ArticleSerializer`의 Inner-class인 `Meta` 클래스에는 model, fields 필드가 있다.  
  model 필드는 어떤 Model을 사용할지에 대한 것이며, fields는 변환할 필드명들을 지정한다.  
  만약 모든 필드를 지정하고 싶으면 배열로 필드명을 하나씩 쓰는것이 아니라, `'__all__'`를 써주면 된다.

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
