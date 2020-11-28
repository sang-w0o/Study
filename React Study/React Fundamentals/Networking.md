<h1>Networking in React</h1>

* React에서 API 요청을 하는 라이브러리는 크게 `Axios`, `Fetch`가 있다.

* 보통의 api들은 path를 제외한 URL의 앞부분이 동일하기에, 만약 이 부분을 따로   
  저장하지 않으면 API Path 가 매우 길어질 수 있다.

* 따라서 대부분은 네트워킹, API만 다루는 파일만 따로 둔다.

* Axios 라이브러리 설치 : `yarn add axios`

* Axios Documentation : <a href="https://github.com/axios/axios">Link</a>

* Axios는 Axios 객체에 대한 설정을 할 수 있다.

* 아래는 공식 문서에 있는 예시이다.
```js
const instance = axios.create({
    baseURL: 'https://some-domain.com/api/',
    timeout: 1000,
    headers: {'X-Custom-Header': 'foobar'}
})
```

* Query Parameter에 URI Encoding을 하려면 아래와 같이 한다.   
  ~~Axios에서 자체적으로 URI Encoding을 지원한다.~~
```js
export const tvApi = {
    topRated: () => api.get("tv/top_rated"),
    popular: () => api.get("tv/popular"),
    airingToday: () => api.get("tv/airing_today"),
    showDetail: (id) => api.get(`tv/${id}`, {
        params: {
            append_to_response: 'videos'
        }
    }),
    search: (term) => api.get('search/tv', {
        params: {
            query: term
        }
    })
};
```