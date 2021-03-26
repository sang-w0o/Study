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
