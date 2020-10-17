<h1>About HTTP</h1>

* ~~HTTP의 정의도 못내리면서 rest api를 개발한단다 했던걸 반성합니다.~~

<h2>HTTP란?</h2>

* `HTTP(Hypertext Transfer Protocol)` is the foundation of World Wide Web, and is used to load web pages using hypertext links.
* HTTP is an `Application Layer` protocol designed to transfer information between networked devices and runs on __top of other__   
  __layers of the network protocol stack__.
* A typical flow over HTTP involves a client machine making a request to a server, which then sends a response message.
<hr/>

<h2>HTTP Request란?</h2>

* An `HTTP Request` is the way internet communications platforms such as web browsers ask for the information they need to load a website.

* Each HTTP Request made accross the internet carries with it a series of encoded data that carries different types of information.   
  A typical HTTP request contains:
  1. HTTP Version Type
  2. A URL
  3. An HTTP Method
  4. HTTP Request Headers
  5. Optional HTTP Body
<hr/>

<h2>About HTTP Method</h2>

* `REST-API` 및 `HTTP/1.1`를 기준으로 작성했습니다.

* `POST` : The `POST` verb is most-often utilized to __CREATE__ new resources. In particular, it's used to create subordinate resources.   
  That is, subordinate to some other(e.g. parent) resource. In other words, when creating a new resource, `POST` to the parent and the   
  service takes card of associating the resource with the parent.

  * `POST` is neither safe nor idempotent. It is therefore recommended for non-idmepotent resource requests.   
    Making two identical `POST` requests will most-likely result in two resources containing the same information.

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

* `GET` : The HTTP `GET` method is used to __read__ a representation of a resource. In the non-error path, `GET` returns a representation in   
  XML or JSON, and an HTTP response code of 200. In an error case, it most often returns a 404(NOT FOUND) or 400(BAD REQUEST).

  * According to the design of HTTP specification, `GET(Along with HEAD)` requests are used __only to read data and not change it__.   
    Therefore, when used this way, they are considered safe. That is, they can be called without risk of data modification or corruption-   
    calling it once has the same effect as calling it 10 times, or none at all. Additionally, `GET(and HEAD)` is idempotent,   
    which means that making multiple identical requests ends up having the same result as a single request.
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

* `PUT` : `PUT` is most-often utilized for __UPDATE__ capabilities. PUT-ing to a known resource URI with the request body containing the   
  newly-updated representation of the original resource.

  * However, `PUT` can also be used to create a resource in the case where the resource ID is chosen by the client instead of by the server.   
    In other words, if the `PUT` is to a URI that contains the value of the non-existent resource ID.

  * Alternatively, use `POST` to create new resources and provide the client-defined ID in the body representation - presumably to   
    a URI that doesn't include the ID of the resource.

  * `PUT` is not a safe operation, in that it modifies(or creates) state on the server, but it is idempotent.   
    In other words, if a client creates or updates a resource using `PUT` and then make that same call again,   
    the resource is still there and still has the same state as it did with the first call.

  * 아래는 `PUT` 요청에 대한 관례적인 응답 형식이다.
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
        <td>204</td>
        <td>NO CONTENT</td>
    </tr>
    <tr>
        <td>404</td>
        <td>NOT FOUND(If ID is not found or invalid.)</td>
    </tr>
    <tr>
        <td>405</td>
        <td>METHOD NOT ALLOWED.(Unless client wants to update/replace every resource in the entire collection.)</td>
    </tr>
</table>

* `PATCH` : `PATCH` is used for __MODIFYING__ capabilities. The `PATCH` request only needs to contain the changes to the resource, __not the complete resource__.
  * Request body of `PUT` contains a set of instructions describing how a resource currently residing on the server should be modified to   
    produce a new version. This means that the `PATCH` body should not just be a modified part of the resource, but in some kind of   
    patch language like JSON Patch or XML Patch.
  * `PATCH` is neither safe or idempotent. However, a `PATCH` request can be issued in such a way as to be idempotent, which also helps   
    prevent bad outcomes from collitions between two `PATCH` requests on the same resource in a similar time frame.   
    Collisions from multiple `PATCH` requests may be more dangerous than `PUT` collisions because some patch formats need to operate from   
    a known base-point or else they will corrup the resource.

  * 아래는 `PATCH` 요청에 대한 관례적인 응답 형식이다.
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
        <td>204</td>
        <td>NO CONTENT</td>
    </tr>
    <tr>
        <td>404</td>
        <td>NOT FOUND</td>
    </tr>
    <tr>
        <td>405</td>
        <td>METHOD NOT ALLOWED(Unless client wants to modify the collection itself.)</td>
    </tr>
</table>

* `DELETE` : `DELETE` is used to __DELETE__ a resource identified by a URI.

  * On successful deletion, return HTTP status 200(OK) along with a response body, perhaps the representation of the deleted item, or a wrapped response. 

  * HTTP-spec-wise, `DELETE` operations are idempotent. If client `DELETE`s a resource, it's removed.   
    Repeatedly calling `DELETE` on that resource ends up the same: the resource is gone. If calling `DELETE` say,   
    decrements a counter(within the resource), the `DELETE` call is no longer idempotent.   
    Usage statistics and measurements may be updated while still considering the service idempotent as long as no resource data is changed.   
    Using `POST` for non-idempotent resource requests is recommended.

  * Calling `DELETE` onn a resource a second time will often return a 404(NOT FOUND), since it was already removed and therefore is no   
    longer findable. This, by some opinions, makes `DELETE` operations no longer idempotent, however, the end-state of the resource is the same.    Returning a 404 is acceptable and communicates accurately the status of the call.

  * 아래는 `DELETE` 요청에 대한 관례적인 응답 형식이다.
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
        <td>NOT FOUND(If ID not found or invalid.)</td>
    </tr>
    <tr>
        <td>405</td>
        <td>METHOD NOT ALLOWED(Unless client wants to delete the whole collection - not often desirable.)</td>
    </tr>
</table>

<hr/>

<h2>HTTP Request Header란?</h2>

* HTTP Headers contain text information stored in key-value pairs, and they are included in every HTTP request(and response, more..).   
  These headers communicate core information, such as what browser the client is using and what data is being requested.
```
Example of Request Headers

:authority : www.google.com
:method : GET
: path : /
: scheme : https
accept : text/html
accept-encoding : gzip, deflate, br
accept-language : en-US, en;q=0.9
upgrade-insecure-requests : 1
user-agent : Mozilla/5.0
```

<h2>HTTP Request Body란?</h2>

* The body of a request is the part that contains the 'body' of information the request is transferring. The body of an HTTP request   
  contains any information being submitted to the web server, such as a username and password, or any other data entered into a form.
<hr/>

<h2>HTTP Response란?</h2>

* An HTTP Response is what web clients receive from an internet server in answer to an HTTP request.   
  These responses communicate valuable information based on what was asked for in the HTTP request.   
  A typical HTTP response contains:
  1. An HTTP Status Cod
  2. HTTP Response Headers
  3. Optional HTTP Body
<hr/>

<h2>HTTP Status Code란?</h2>

* HTTP Status Codes are 3-digit codes most often used to indicate whether an HTTP request has been successfully completed. Status codes are broken into the following 5 blocks:
  * `1xx` : Informational
  * `2xx` : Success
  * `3xx` : Redirection
  * `4xx` : Client Error
  * `5xx` : Server Error

* The `xx` refers to different numbers between 00 and 99. 
* Status codes starting with the number `2` indicate a success. For example, `200` stands for OK, indicating the request was properly completed.
* If the response starts with a `4` or `5`, that means there was an error and the desired work will not be done.   
  A status code that begins with a `4` indicated a client-side error. A status code that begins with a `5` means something went wrong on the    server side. Status codes can also begin with a `1` or `3`, which indicate an informational response and a redirect, respectively.
<hr/>

