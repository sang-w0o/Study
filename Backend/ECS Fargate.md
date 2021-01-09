* 우선 기본적으로 Github Action에 실행되는 `aws.yml` 입니다.
```yml
on:
  push:
    branches: [ dev2 ]
    

name: Deploy to Amazon ECS

jobs:
  deploy:
    name: Deploy
    runs-on: ubuntu-latest

    steps:
    - name: Checkout
      uses: actions/checkout@v2

    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v1
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ secrets.AWS_S3_REGION }}

    - name: build
      run: ./gradlew build -x test
      env:
        PRD_WH_DATASOURCE_URL: ${{ secrets.PRD_WH_DATASOURCE_URL }}
        TEST_WH_DATASOURCE_URL: ${{ secrets.TEST_WH_DATASOURCE_URL }}
        WH_DATASOURCE_USERNAME: ${{ secrets.WH_DATASOURCE_USERNAME }}
        WH_DATASOURCE_PASSWORD: ${{ secrets.WH_DATASOURCE_PASSWORD }}
        AWS_ACCESS_KEY_ID: ${{ secrets.BANCHANGO_AWS_ACCESS_KEY_ID }}
        AWS_SECRET_ACCESS_KEY: ${{ secrets.BANCHANGO_AWS_SECRET_ACCESS_KEY }}
        AWS_S3_BUCKET: ${{ secrets.BANCHANGO_AWS_S3_BUCKET }}
        AWS_S3_REGION: ${{ secrets.BANCHANGO_AWS_S3_REGION }}
        WH_DEFAULT_IMAGE_URL: ${{ secrets.WH_DEFAULT_IMAGE_URL }}
        AWS_EMAIL_ACCESS_KEY: ${{ secrets.AWS_EMAIL_ACCESS_KEY }}
        AWS_EMAIL_SECRET_ACCESS_KEY: ${{ secrets.AWS_EMAIL_SECRET_ACCESS_KEY }}
        
    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v1

    - name: Build, tag, and push image to Amazon ECR
      id: build-image
      env:
        ECR_REGISTRY: ${{ secrets.AWS_ECR_REPOSITORY }}
        ECR_REPOSITORY: banchango_server
        IMAGE_TAG: ${{ github.sha }}
      run: |
        # Build a docker container and
        # push it to ECR so that it can
        # be deployed to ECS.
        docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
        echo "::set-output name=image::$ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG"
    - name: Fill in the new image ID in the Amazon ECS task definition
      id: task-def
      uses: aws-actions/amazon-ecs-render-task-definition@v1
      with:
        task-definition: task-definition.json
        container-name: BanchangoContainer
        image: ${{ steps.build-image.outputs.image }}

    - name: Deploy Amazon ECS task definition
      uses: aws-actions/amazon-ecs-deploy-task-definition@v1
      with:
        task-definition: ${{ steps.task-def.outputs.task-definition }}
        service: BanchangoService
        cluster: BanchangoCluster
        codedeploy-deployment-group: BanchangoECSDeploymentGroup
        wait-for-service-stability: true
```

* 위 코드는 `Gradle build` 하는 부분을 빼고는 기본적으로 제공되는 템플릿을 사용했습니다.

* `task-definition.json`은 아래와 같습니다.
```json
{
    "executionRoleArn": "arn:aws:iam::598334522273:role/ecsAutoscaleRole",
    "containerDefinitions": [{
      "name": "BanchangoContainer",
      "image": "598334522273.dkr.ecr.ap-northeast-2.amazonaws.com/banchango_server",
      "essential": true,
      "portMappings": [{
        "hostPort": 8080,
        "protocol": "tcp",
        "containerPort": 8080
      }],
      "secrets": [{
        "name": "AWS_ACCESS_KEY_ID",
        "valueFrom": "ACCESS_KEY_ID"
      }]    
    }],
    "requiresCompatibilities": [
      "FARGATE"
    ],
    "networkMode": "awsvpc",
    "cpu": "512",
    "memory": "1024",
    "family": "BanchangoTask"
}
```

* 마지막으로 `appspec.yaml`은 아래와 같습니다.
```yaml
version: 0.0
Resources:
  - TargetService:
      Type: AWS::ECS::Service
      Properties:
        TaskDefinition: "arn:aws:ecs:ap-northeast-2:598334522273:task-definition/BanchangoTask:7"
        LoadBalancerInfo:
          ContainerName: "BanchangoContainer"
          ContainerPort: 8080
        PlatformVersion: "LATEST"
```

* 우선 제가 이해한 바로는 `task-definition.json`은 작업 정의를 하는 파일로 알고 있습니다.   

* 우선, 가장 큰 문제점은 Blue/Green Deployment가 1단계에서 50%에 도달하면 무제한으로 대기를 합니다.   
  개인적으로 이 부분은 `task-definition.json` 또는 CodeDeploy의 배포 그룹에 문제가 있는 것 같습니다...

* 그리구