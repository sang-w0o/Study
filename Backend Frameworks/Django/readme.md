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
