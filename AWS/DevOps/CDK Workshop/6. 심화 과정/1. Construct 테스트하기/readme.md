# Construct 테스트하기

## CDK assert library

- 이번에는 `aws-cdk-lib/assertions` 라이브러리를 사용할 것이다. 이 라이브러리는 단위 테스트 및 통합 테스트를 작성하는 데 유용한 여러  
  helper 함수들을 담고 있다.

- 대부분의 경우에는 `hasResourceProperties()` 함수를 사용할 것이다. 이 helper 함수는 특정 타입의 리소스가 존재하는지, 그리고 그 리소스의  
  property들 중 일부가 특정 값으로 설정되어 있는지를 단언한다.

```js
template.hasResourceProperties("AWS::CertificateManager::Certificate", {
  DomainName: "test.example.com",
  ShouldNotExist: Match.absent(),
});
```

- `Match.absent()`는 특정 property가 값이 설정되지 않았음을 단언하고 싶을 때 사용한다.

- [@aws-cdk/assertions 공식 문서](https://docs.aws.amazon.com/cdk/api/v1/docs/assertions-readme.html)
