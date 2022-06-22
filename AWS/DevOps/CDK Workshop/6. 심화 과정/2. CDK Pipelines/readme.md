# CDK Pipelines

- 이번에는 이전에 작성한 애플리케이션에 CD(Continuous Deployment) pipeline을 생성해보자.

- CD는 대부분의 웹 프로젝트에서 중요한 컴포넌트이지만, 설정하기가 어려울 수 있다.  
  [CDK Pipelines](https://docs.aws.amazon.com/cdk/v2/guide/cdk_pipeline.html) construct는 이 pipeline을 만드는 프로세스를 쉽고 빠르게 할 수 있게 해준다.

- CDK Pipeline를 이 문서에서는 AWS CodeCommit으로 구현하지만, 간단히 Github Actions로 구축할 것이다.  
  배포 스크립트는 아래와 같다.

```yml
on: [push]
jobs:
  aws_cdk:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repo
        uses: actions/checkout@v3
      - uses: actions/setup-node@v2
        with:
          node-version: "14"
      - name: Configure aws credentials
        uses: aws-actions/configure-aws-credentials@master
        with:
          aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
          aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
          aws-region: "ap-northeast-2"
      - name: Install dependencies
        run: yarn install
      - name: Synthesize stack
        run: yarn cdk synth
      - name: Deploy stack
        run: yarn cdk deploy --all --require-approval never
```

---
