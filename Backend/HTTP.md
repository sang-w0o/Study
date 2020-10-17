<h1>About HTTP</h1>

* ~~HTTP의 정의도 못내리면서 rest api를 개발한단다 했던걸 반성합니다.~~

<h2>HTTP란?</h2>

* `HTTP(Hypertext Transfer Protocol)` is the foundation of World Wide Web, and is used to load web pages using hypertext links.
* HTTP is an `Application Layer` protocol designed to transfer information between networked devices and runs on __top of other layers of the network protocol stack__.
* A typical flow over HTTP involves a client machine making a request to a server, which then sends a response message.
<hr/>

<h2>HTTP Request란?</h2>

* An `HTTP Request` is the way internet communications platforms such as web browsers ask for the information they need to load a website.

* Each HTTP Request made accross the internet carries with it a series of encoded data that carries different types of information. A typical HTTP request contains:
  1. HTTP Version Type
  2. A URL
  3. An HTTP Method
  4. HTTP Request Headers
  5. Optional HTTP Body

<h3>About HTTP Method</h3>

* `REST-API`를 기준으로 작성했습니다.

* `POST` : The `POST` verb is most-often utilized to __CREATE__ new resources. In particular, it's used to create subordinate resources. That is, subordinate to some other(e.g. parent) resource. In other words, when creating a new resource, `POST` to the parent and the service takes card of associating the resource with the parent.

  * `POST` is neither safe nor idempotent. It is therefore recommended for non-idmepotent resource requests. Making two identical `POST` requests will most-likely result in two resources containing the same information.

  * 아래는 `POST` 요청에 대한 관례적인 응답 형식이다.
<table>
    <tr>
        <td>Response Code</td>
        <td>Meanings</td>
    </tr>
    <tr>
        <td>201</td>
        <td>CREATED</td>
    </tr>
    <tr>
        <td>404</td>
        <td>NOT FOUND</td>
    </tr>
    <tr>
        <td>409</td>
        <td>CONFLICT(If resource already exists.)</td>
    </tr>
</table>

* `GET` : The HTTP `GET` method is used to __read__ a representation of a resource. In the non-error path, `GET` returns a representation in XML or JSON, and an HTTP response code of 200. In an error case, it most often returns a 404(NOT FOUND) or 400(BAD REQUEST).

  * According to the design of HTTP specification, `GET(Along with HEAD)` requests are used __only to read data and not change it__. Therefore, when used this way, they are considered safe. That is, they can be called without risk of data modification or corruption - calling it once has the same effect as calling it 10 times, or none at all. Additionally, `GET(and HEAD)` is idempotent, which means that making multiple identical requests ends up having the same result as a single request.
    * __DO NOT expose unsafe operations via `GET` - it should NEVER MODIFY ANY RESOURCES ON THE SERVER__.

  * 아래는 `GET` 요청에 대한 관례적인 응답 형식이다.
<table>
    <tr>
        <td>Response Code</td>
        <td>Meanings</td>
    </tr>
    <tr>
        <td>200</td>
        <td>OK</td>
    </tr>
    <tr>
        <td>404</td>
        <td>NOT FOUND</td>
    </tr>
</table>

* `POST` : 