# Lambda로 이미지 압축 구현하기

<h2>들어가며</h2>

- 웹 애플리케이션 및 모바일 애플리케이션에는 사진 등의 다양한 미디어 파일이 굉장히 자주 사용된다.  
  이때, 사용자로부터 파일을 받아서 보여줘야하는 서비스가 있다고 해보자.  
  웹에서 사용자로부터 사진을 받으면 해당 파일을 백엔드 서비스에 전달하고,  
  백엔드 서비스에서는 받은 파일을 S3와 같은 Storage Service에 저장할 것이다.

- 이때, 사용자는 꽤나 용량이 큰 사진을 올릴 수 있을 것이다.  
  이렇게 용량이 큰 사진을 받아 그대로 Storage Service에 저장하면 나중에 해당 사진을  
  렌더링할 때 시간이 오래 걸린다는 단점이 있다.  
  하지만 이를 줄이기 위해 특정 크기 이상의 사진을 업로드하지 못하게 한다면, UX가 좋아지지 않는  
  것 까지 이어질 수도 있다. 이를 해결하기 위해 아래의 아키텍쳐를 구현해보자.

![picture 1](../../images/4a6695fc00e67f4ba002367cc517825fec0ba0259fc7c590cb2bdb77edfdd6a0.png)

- 참고로 이 과정에서 백엔드 서비스는 생략한다.

<hr/>

<h2>Lambda 구축</h2>

- Lambda가 수행할 것은 매우 간단한데, 이미지를 받은 다음 해당 이미지를 압축한 후, S3에 업로드하는 것이다.  
  Lambda의 배포 과정 개발에 대한 것은 <a href="https://github.com/sang-w0o/Study/blob/master/AWS/Backend/Lambda%20%EA%B5%AC%EC%B6%95%ED%95%98%EA%B8%B0.md">여기</a>에서 확인할 수 있다.

- 아래는 Lambda의 함수가 특정 로직을 처리하는 pseudo code이다.

```ts
const upload: Handler = async (event: APIGatewayProxyEvent) => {
  const file = await parseFile(event);  // 파일을 받아온다.
  const response = await uploadToS3(file);  // S3에 업로드한다.
  return Response;  // 응답 반환
  };
};
```

- 이제 하나씩 구현해보자.

<h3>API Gateway 구축</h3>

- API Gateway는 간단히 트리거 추가를 통해 하면 되는데, 이때 주의할 점은 API의 설정에서 아래처럼  
  이진 형식에 `mutlipart/form-data`를 설정해주는 것이다.

- API Gateway를 먼저 만들고 Lambda에 연결하면 `multipart/form-data`를 선택해준 사항이  
  없어지는 것 같다.(버그인가..?)  
  따라서 Lambda를 먼저 배포한 후, Lambda 콘솔에서 _트리거 추가_ 버튼을 통해 API Gateway를 설정해주자.  
  _추가 세팅_ 항목에서 설정할 수 있다.
  ![picture 2](../../images/e98128c78e95432e037b2e6fd91eab330129cb59fb7da94e4f6f3ac3fab83dea.png)

<hr/>

<h2>Lambda 함수 구축</h2>

- aws-lambda 패키지가 제공하는 Handler함수는 event를 인자로 받아오는데,  
  이때 클라이언트가 보내는 `multipart/form-data`를 어떻게 파싱해서 사용할지를 생각해야 한다.  
  이를 위한 패키지로 `lambda-multipart-parser`를 선택했다.

- 이제 사진 파일을 가져올 방법은 정했으니, _압축_ 을 어떻게 진행해야 할지 생각해야 한다.  
  사진 압축을 해주는 유명한 오픈소스 라이브러리로는 `compressorjs`와 `sharp`가 있는데,  
  Lambda에서 Typescript를 원활하게 사용하기 위해 <a href="https://github.com/lovell/sharp">Sharp</a>를 선택했다.

- 이제 마지막으로 S3에 업로드를 어떻게 진행할지를 생각해야 한다.  
  당연히 aws가 공식적으로 제공하는 aws-sdk 패키지를 사용하여 진행하는것이 가장 좋다.

<h3>event로부터 사진 가져오기</h3>

- `lambda-multipart-parser`가 제공하는 함수를 사용하면, event 객체로부터  
  `multipart/form-data` 형식의 파일(사진)을 쉽게 파싱하여 꺼내올 수 있다.

```ts
// event에서 이미지 파싱하여 반환
const parseFile = async (
  event: APIGatewayProxyEvent
): Promise<parser.MultipartFile> => {
  const parsedFile = await parser.parse(event);
  const file = parsedFile.files[0];
  return file;
};
```

<h3>사진 압축하기</h3>

- `sharp` 라이브러리를 사용하면 사진 압축 또한 쉽게 진행할 수 있다.  
  lambda함수에 연결된 trigger인 API Gateway를 고도화하여 압축률 등을  
  사용자가 선택할 수 있도록 고도화할 수 있지만, 간단히 sharp가 제공하는 기본값을  
  사용하도록 진행했다.

```ts
// 사진을 받아와서 압축한 후 반환
export const compress = async (
  bits: Buffer,
  options?: CompressOptions
): Promise<ImageDataAndInfo> => {
  const { data, info } = await sharp(bits)
    .resize(options?.width, options?.height)
    .webp()
    .toBuffer({ resolveWithObject: true });

  return {
    data,
    info,
  };
};
```

- 위 코드에 사용된 `ImageDataAndInfo`와 `CompressOptions`는 직접 정의한 간단한 인터페이스이다.

```ts
export interface CompressOptions {
  width?: number;
  height?: number;
}

interface ImageDataAndInfo {
  data: Buffer;
  info: OutputInfo;
}
```

<h3>S3에 업로드하기</h3>

- 이제 사진을 꺼내오고, 압축하는 과정 까지 작성했으니 S3에 업로드하는 과정을 살펴보자.

```ts
const uploadToS3 = async (
  beforeFile: parser.MultipartFile
): Promise<Response> => {
  AWS.config.update({
    region: "ap-northeast-2",
    credentials: new AWS.Credentials({
      accessKeyId: AWS_S3_ACCESS_KEY_ID,
      secretAccessKey: AWS_S3_SECRET_ACCESS_KEY,
    }),
  });

  const s3 = new AWS.S3({ params: { Bucket: AWS_S3_BUCKET } });

  const compressedFile = await compress(beforeFile.content);

  const putObjectRequest: PutObjectRequest = {
    Key: beforeFile.filename,
    Bucket: AWS_S3_BUCKET,
    Body: compressedFile.data,
  };

  const result = await s3.upload(putObjectRequest).promise();

  const response: Response = {
    url: result.Location,
  };

  return response;
};
```

- `AWS.config.update()`는 이 aws sdk가 사용할 aws 환경을 설정해준 것이다.  
  설정된 값은 S3에 대한 적절한 권한을 가진 IAM 사용자의 credential들이다.

- `putObjectRequest`는 s3에 업로드하기 위한 요청 정보들을 담고 있다.  
  `Key`에 지정한 값은 bucket 내의 객체명이 되고, `Bucket`은 bucket의 이름이고  
  `Body`는 요청에 보낼 객체의 실제 content이다.

- 이 예시에서는 객체가 업로드된 S3의 url을 반환하는 것이 목표이므로 마지막에  
  `s3.upload()`를 호출하여 url을 가져올 수 있는 객체를 받아놨다.

<h3>handler 함수</h3>

- 이제 위에서 작성한 함수들을 실제 lambda가 trigger되면 실행할 함수에서 사용하도록 하기만 하면 된다.

```ts
const upload: Handler = async (event: APIGatewayProxyEvent) => {
  const file = await parseFile(event);
  const response = await uploadToS3(file);
  return {
    statusCode: 200,
    body: JSON.stringify(response),
    headers: {
      "Access-Control-Allow-Headers": "*",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Methods": "HEAD,OPTIONS,POST,GET",
    },
  };
};
```

- 반환하는 값에 header 정보를 추가해준 이유는 CORS를 허용하기 위함이다.

<hr/>

- 이제 요청을 보내면 결과가 아래와 같이 잘 나오는 것을 확인할 수 있다.

![picture 3](../../images/5efe1f49fb13e44adce6175f52ae0de6d7ff4442fd353b889c5a1ef162056a99.png)

- 모든 소스 코드는 <a href="https://github.com/Example-Collection/Lambda-Image-Resizing-Example">이 repository</a>에서 확인할 수 있으며, 실행 과정은 해당  
  repository의 readme를 참고하길 바란다.
