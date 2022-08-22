# EKS 배포하기

- 이 글은 사이드 프로젝트로 개발하는 Spring Boot application을 EKS로 배포했던 과정을 설명합니다.

- 모든 과정은 [AWS Builders 2022 - EKS](https://awskocaptain.gitbook.io/aws-builders-eks/)를 참고해 진행되었으므로, 이 글의 과정과 매우 유사합니다.

## (1) - 자격 증명 및 환경 설정

- aws cli가 설치되어 있다는 가정 하에, AWS console에서 EKS를 사용하기 위한 권한 등을 포함한 IAM 사용자를 생성하고,  
  로컬 터미널에서 credential을 설정합니다.

- 저의 경우, 테스트를 위해 `AdministratorAccess` 권한을 부여했습니다.

- kubectl, eksctl, k9s는 환경에 맞게 설치해줍니다.

### KMS 설정

- 암호화 key를 K8S에서 편리하기 사용하기 위해 손쉽게 암호화 key를 생성, 관리해주며 다양한 AWS의 서비스들과 연동할 수 있도록 하는  
  AWS KMS를 사용하도록 합니다.

  ```sh
  aws kms create-alias --alias-name alias/planit-eks --target-key-id $(aws kms create-key --query KeyMetadata.Arn --output text)
  ```

- AWS Console에서 KMS에 접속해 생성된 CMK(Customer-Managed Key) 정보를 확인합니다.

  ![picture 5](/images/AWS_DEVOPS_EKS_1.png)

- 생성된 CMK의 ARN을 사용해 접근하게 되므로, 아래처럼 환경 변수로 지정해준 후 별도의 파일에 저장하도록 합니다.

  ```sh
  export MASTER_ARN=$(aws kms describe-key --key-id alias/planit-eks --query KeyMetadata.Arn --output text)
  echo $MASTER_ARN > master_arn.txt
  ```

---

## (2) - VPC 설정

![picture 6](/images/AWS_DEVOPS_EKS_2.png)

- 위 사진과 같은 구성을 eksctl과 cloudformation으로 구성해볼 것이다.

- Cloudformation이 사용하는 yml파일에는 VPC, AZ, Subnet, Routing Table등을 구성하고, eksctl로 사전에 정의된 yaml을  
  통해 public node group, 그리고 private node group을 구성한다.

- 여기서 생성할 VPC에는 NAT Gateway가 3개 사용되므로 총 3개의 EIP가 사용된다.  
  기본적으로 한 계정에서 사용할 수 있는 EIP의 최대 개수는 5개 이므로 EIP가 3개 이상 여유가 있어야 배포가 가능하다.

<details><summary>Cloudformation yml 파일은 아래와 같다.(EKSVPC3AZ.yml)</summary>

<p>

- 생성되는 리소스는 다음과 같다.

  - VPC
  - 3개의 private subnet
  - 3개의 public subnet
  - Internet Gateway
  - 3개의 NAT Gateway
  - 3개의 Public Route Table
  - 3개의 Private Route Table
  - ControlPlane을 위한 security group

```yml
---
AWSTemplateFormatVersion: "2010-09-09"
Description: "Amazon EKS Sample VPC - 3 AZ, Private 3 subnets, Public 3 subnets, 1 IGW, 3 NATGateways, Public RT, 3 Private RT, Security Group for ControlPlane "

Metadata:
  AWS::CloudFormation::Interface:
    ParameterGroups:
      - Label:
          default: "Worker Network Configuration"
        Parameters:
          - VpcBlock
          - AvailabilityZoneA
          - AvailabilityZoneB
          - AvailabilityZoneC
          - PublicSubnet01Block
          - PublicSubnet02Block
          - PublicSubnet03Block
          - PrivateSubnet01Block
          - PrivateSubnet02Block
          - PrivateSubnet03Block
          - TGWSubnet01Block
          - TGWSubnet02Block
          - TGWSubnet03Block

Parameters:
  VpcBlock:
    Type: String
    Default: 10.11.0.0/16
    Description: The CIDR range for the VPC. This should be a valid private (RFC 1918) CIDR range.

  AvailabilityZoneA:
    Description: "Choose AZ1 for your VPC."
    Type: AWS::EC2::AvailabilityZone::Name
    Default: "ap-northeast-2a"

  AvailabilityZoneB:
    Description: "Choose AZ2 for your VPC."
    Type: AWS::EC2::AvailabilityZone::Name
    Default: "ap-northeast-2b"

  AvailabilityZoneC:
    Description: "Choose AZ1 for your VPC."
    Type: AWS::EC2::AvailabilityZone::Name
    Default: "ap-northeast-2c"

  PublicSubnet01Block:
    Type: String
    Default: 10.11.0.0/20
    Description: CidrBlock for public subnet 01 within the VPC

  PublicSubnet02Block:
    Type: String
    Default: 10.11.16.0/20
    Description: CidrBlock for public subnet 02 within the VPC

  PublicSubnet03Block:
    Type: String
    Default: 10.11.32.0/20
    Description: CidrBlock for public subnet 03 within the VPC

  PrivateSubnet01Block:
    Type: String
    Default: 10.11.48.0/20
    Description: CidrBlock for private subnet 01 within the VPC

  PrivateSubnet02Block:
    Type: String
    Default: 10.11.64.0/20
    Description: CidrBlock for private subnet 02 within the VPC

  PrivateSubnet03Block:
    Type: String
    Default: 10.11.80.0/20
    Description: CidrBlock for private subnet 03 within the VPC

  TGWSubnet01Block:
    Type: String
    Default: 10.11.251.0/24
    Description: CidrBlock for TGW subnet 01 within the VPC

  TGWSubnet02Block:
    Type: String
    Default: 10.11.252.0/24
    Description: CidrBlock for TGW subnet 02 within the VPC

  TGWSubnet03Block:
    Type: String
    Default: 10.11.253.0/24
    Description: CidrBlock for TGW subnet 03 within the VPC

Resources:
  #####################
  # Create-VPC : VPC #
  #####################

  VPC:
    Type: AWS::EC2::VPC
    Properties:
      CidrBlock: !Ref VpcBlock
      EnableDnsSupport: true
      EnableDnsHostnames: true
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}"

  ########################################################
  # Create-InternetGateway:
  ########################################################

  InternetGateway:
    Type: "AWS::EC2::InternetGateway"

  ########################################################
  # Attach - VPC Gateway
  ########################################################

  VPCGatewayAttachment:
    Type: "AWS::EC2::VPCGatewayAttachment"
    Properties:
      InternetGatewayId: !Ref InternetGateway
      VpcId: !Ref VPC

  ########################################################
  # Create-Public-Subnet: PublicSubnet01,02,03,04
  ########################################################

  PublicSubnet01:
    Type: AWS::EC2::Subnet
    Metadata:
      Comment: Public Subnet 01
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Ref PublicSubnet01Block
      AvailabilityZone: !Ref AvailabilityZoneA
      MapPublicIpOnLaunch: "true"
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-PublicSubnet01"
        - Key: kubernetes.io/role/elb
          Value: 1

  PublicSubnet02:
    Type: AWS::EC2::Subnet
    Metadata:
      Comment: Public Subnet 02
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Ref PublicSubnet02Block
      AvailabilityZone: !Ref AvailabilityZoneB
      MapPublicIpOnLaunch: "true"
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-PublicSubnet02"
        - Key: kubernetes.io/role/elb
          Value: 1

  PublicSubnet03:
    Type: AWS::EC2::Subnet
    Metadata:
      Comment: Public Subnet 03
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Ref PublicSubnet03Block
      AvailabilityZone: !Ref AvailabilityZoneC
      MapPublicIpOnLaunch: "true"
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-PublicSubnet03"
        - Key: kubernetes.io/role/elb
          Value: 1

  #####################################################################
  # Create-Public-RouteTable:
  #####################################################################

  PublicRouteTable:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref VPC
      Tags:
        - Key: Name
          Value: Public Subnets
        - Key: Network
          Value: PublicRT

  ################################################################################################
  # Associate-Public-RouteTable: VPC_Private_Subnet_a,b Accsociate VPC_Private_RouteTable #
  ################################################################################################
  PublicSubnet01RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PublicSubnet01
      RouteTableId: !Ref PublicRouteTable

  PublicSubnet02RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PublicSubnet02
      RouteTableId: !Ref PublicRouteTable

  PublicSubnet03RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PublicSubnet03
      RouteTableId: !Ref PublicRouteTable

  ################################################################################################
  # Create Public Routing Table
  ################################################################################################
  PublicRoute:
    DependsOn: VPCGatewayAttachment
    Type: AWS::EC2::Route
    Properties:
      RouteTableId: !Ref PublicRouteTable
      DestinationCidrBlock: 0.0.0.0/0
      GatewayId: !Ref InternetGateway

  ################################################################################################
  # Create-NATGateway: NATGATEWAY01,02,03
  ################################################################################################
  NatGateway01:
    DependsOn:
      - NatGatewayEIP1
      - PublicSubnet01
      - VPCGatewayAttachment
    Type: AWS::EC2::NatGateway
    Properties:
      AllocationId: !GetAtt "NatGatewayEIP1.AllocationId"
      SubnetId: !Ref PublicSubnet01
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-NatGatewayAZ1"

  NatGateway02:
    DependsOn:
      - NatGatewayEIP2
      - PublicSubnet02
      - VPCGatewayAttachment
    Type: AWS::EC2::NatGateway
    Properties:
      AllocationId: !GetAtt "NatGatewayEIP2.AllocationId"
      SubnetId: !Ref PublicSubnet02
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-NatGatewayAZ2"

  NatGateway03:
    DependsOn:
      - NatGatewayEIP3
      - PublicSubnet03
      - VPCGatewayAttachment
    Type: AWS::EC2::NatGateway
    Properties:
      AllocationId: !GetAtt "NatGatewayEIP3.AllocationId"
      SubnetId: !Ref PublicSubnet03
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-NatGatewayAZ3"

  NatGatewayEIP1:
    DependsOn:
      - VPCGatewayAttachment
    Type: "AWS::EC2::EIP"
    Properties:
      Domain: vpc

  NatGatewayEIP2:
    DependsOn:
      - VPCGatewayAttachment
    Type: "AWS::EC2::EIP"
    Properties:
      Domain: vpc

  NatGatewayEIP3:
    DependsOn:
      - VPCGatewayAttachment
    Type: "AWS::EC2::EIP"
    Properties:
      Domain: vpc

  ########################################################
  # Create-Security-Group : ControlPlane
  ########################################################
  ControlPlaneSecurityGroup:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupDescription: Cluster communication with worker nodes
      VpcId: !Ref VPC

  ########################################################
  # Create-Security-Group : Session Manager
  ########################################################
  SSMSG:
    Type: AWS::EC2::SecurityGroup
    Properties:
      GroupDescription: Open-up ports for HTTP/S from All network
      GroupName: SSMSG
      VpcId: !Ref VPC
      SecurityGroupIngress:
        - IpProtocol: tcp
          CidrIp: 0.0.0.0/0
          FromPort: "80"
          ToPort: "80"
        - IpProtocol: tcp
          CidrIp: 0.0.0.0/0
          FromPort: "443"
          ToPort: "443"
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-SSMSG"
  ########################################################
  # Create-Private-Subnet: PrivateSubnet01,02
  ########################################################

  PrivateSubnet01:
    Type: AWS::EC2::Subnet
    Metadata:
      Comment: Private Subnet 01
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Ref PrivateSubnet01Block
      AvailabilityZone: !Ref AvailabilityZoneA
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-PrivateSubnet01"
        - Key: kubernetes.io/role/internal-elb
          Value: 1

  PrivateSubnet02:
    Type: AWS::EC2::Subnet
    Metadata:
      Comment: Private Subnet 02
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Ref PrivateSubnet02Block
      AvailabilityZone: !Ref AvailabilityZoneB
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-PrivateSubnet02"
        - Key: kubernetes.io/role/internal-elb
          Value: 1

  PrivateSubnet03:
    Type: AWS::EC2::Subnet
    Metadata:
      Comment: Private Subnet 03
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Ref PrivateSubnet03Block
      AvailabilityZone: !Ref AvailabilityZoneC
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-PrivateSubnet03"
        - Key: kubernetes.io/role/internal-elb
          Value: 1

  #####################################################################
  # Create-Private-RouteTable: PrivateRT01,02
  #####################################################################
  PrivateRouteTable01:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref VPC
      Tags:
        - Key: Name
          Value: Private Subnet AZ1
        - Key: Network
          Value: PrivateRT01

  PrivateRouteTable02:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref VPC
      Tags:
        - Key: Name
          Value: Private Subnet AZ2
        - Key: Network
          Value: PrivateRT02

  PrivateRouteTable03:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref VPC
      Tags:
        - Key: Name
          Value: Private Subnet AZ3
        - Key: Network
          Value: PrivateRT03

  ################################################################################################
  # Associate-Private-RouteTable: VPC_Private_Subnet_a,b Accsociate VPC_Private_RouteTable #
  ################################################################################################

  PrivateSubnet01RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PrivateSubnet01
      RouteTableId: !Ref PrivateRouteTable01

  PrivateSubnet02RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PrivateSubnet02
      RouteTableId: !Ref PrivateRouteTable02

  PrivateSubnet03RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref PrivateSubnet03
      RouteTableId: !Ref PrivateRouteTable03

  ################################################################################################
  # Add Prviate Routing Table
  ################################################################################################

  PrivateRoute01:
    DependsOn:
      - VPCGatewayAttachment
      - NatGateway01
    Type: AWS::EC2::Route
    Properties:
      RouteTableId: !Ref PrivateRouteTable01
      DestinationCidrBlock: 0.0.0.0/0
      NatGatewayId: !Ref NatGateway01

  PrivateRoute02:
    DependsOn:
      - VPCGatewayAttachment
      - NatGateway02
    Type: AWS::EC2::Route
    Properties:
      RouteTableId: !Ref PrivateRouteTable02
      DestinationCidrBlock: 0.0.0.0/0
      NatGatewayId: !Ref NatGateway02

  PrivateRoute03:
    DependsOn:
      - VPCGatewayAttachment
      - NatGateway03
    Type: AWS::EC2::Route
    Properties:
      RouteTableId: !Ref PrivateRouteTable03
      DestinationCidrBlock: 0.0.0.0/0
      NatGatewayId: !Ref NatGateway03
  ########################################################
  # Create-TGW-Subnet: TGWSubnet01,02,03
  ########################################################

  TGWSubnet01:
    Type: AWS::EC2::Subnet
    Metadata:
      Comment: TGW Subnet 01
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Ref TGWSubnet01Block
      AvailabilityZone: !Ref AvailabilityZoneA
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-TGWSubnet01"
        - Key: kubernetes.io/role/internal-elb
          Value: 1

  TGWSubnet02:
    Type: AWS::EC2::Subnet
    Metadata:
      Comment: TGW Subnet 02
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Ref TGWSubnet02Block
      AvailabilityZone: !Ref AvailabilityZoneB
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-TGWSubnet02"
        - Key: kubernetes.io/role/internal-elb
          Value: 1

  TGWSubnet03:
    Type: AWS::EC2::Subnet
    Metadata:
      Comment: TGW Subnet 03
    Properties:
      VpcId: !Ref VPC
      CidrBlock: !Ref TGWSubnet03Block
      AvailabilityZone: !Ref AvailabilityZoneC
      Tags:
        - Key: Name
          Value: !Sub "${AWS::StackName}-TGWSubnet03"
        - Key: kubernetes.io/role/internal-elb
          Value: 1

  #####################################################################
  # Create-TGW-RouteTable: TGWRT01,02,03,04
  #####################################################################
  TGWRouteTable01:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref VPC
      Tags:
        - Key: Name
          Value: TGW Subnet AZ1
        - Key: Network
          Value: TGWRT01

  TGWRouteTable02:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref VPC
      Tags:
        - Key: Name
          Value: TGW Subnet AZ2
        - Key: Network
          Value: TGWRT02

  TGWRouteTable03:
    Type: AWS::EC2::RouteTable
    Properties:
      VpcId: !Ref VPC
      Tags:
        - Key: Name
          Value: TGW Subnet AZ3
        - Key: Network
          Value: TGWRT03

  ################################################################################################
  # Associate-TGW-RouteTable: VPC_TGW_Subnet_a,b Accsociate VPC_TGW_RouteTable #
  ################################################################################################

  TGWSubnet01RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref TGWSubnet01
      RouteTableId: !Ref TGWRouteTable01

  TGWSubnet02RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref TGWSubnet02
      RouteTableId: !Ref TGWRouteTable02

  TGWSubnet03RouteTableAssociation:
    Type: AWS::EC2::SubnetRouteTableAssociation
    Properties:
      SubnetId: !Ref TGWSubnet03
      RouteTableId: !Ref TGWRouteTable03

  ######################################################################
  # Create-System-Manager-Endpoint: Create VPC SystemManager Endpoint #
  ######################################################################

  SSMEndpoint:
    Type: AWS::EC2::VPCEndpoint
    Properties:
      VpcId: !Ref VPC
      ServiceName: !Sub "com.amazonaws.${AWS::Region}.ssm"
      VpcEndpointType: Interface
      PrivateDnsEnabled: True
      SubnetIds:
        - Ref: PrivateSubnet01
        - Ref: PrivateSubnet02
        - Ref: PrivateSubnet03
      SecurityGroupIds:
        - Ref: SSMSG

  SSMMEndpoint:
    Type: AWS::EC2::VPCEndpoint
    Properties:
      VpcId: !Ref VPC
      ServiceName: !Sub "com.amazonaws.${AWS::Region}.ssmmessages"
      VpcEndpointType: Interface
      PrivateDnsEnabled: True
      SubnetIds:
        - Ref: PrivateSubnet01
        - Ref: PrivateSubnet02
        - Ref: PrivateSubnet03
      SecurityGroupIds:
        - Ref: SSMSG

Outputs:
  VpcId:
    Description: The VPC Id
    Value: !Ref VPC

  PublicSubnet01:
    Description: PublicSubnet01 ID in the VPC
    Value: !Ref PublicSubnet01

  PublicSubnet02:
    Description: PublicSubnet02 ID in the VPC
    Value: !Ref PublicSubnet02

  PublicSubnet03:
    Description: PublicSubnet03 ID in the VPC
    Value: !Ref PublicSubnet03

  PrivateSubnet01:
    Description: PrivateSubnet01 ID in the VPC
    Value: !Ref PrivateSubnet01

  PrivateSubnet02:
    Description: PrivateSubnet02 ID in the VPC
    Value: !Ref PrivateSubnet02

  PrivateSubnet03:
    Description: PrivateSubnet03 ID in the VPC
    Value: !Ref PrivateSubnet03

  SecurityGroups:
    Description: Security group for the cluster control plane communication with worker nodes
    Value: !Join [",", [!Ref ControlPlaneSecurityGroup]]

  TGWSubnet01:
    Description: TGWSubnet01 ID in the VPC
    Value: !Ref TGWSubnet01

  TGWSubnet02:
    Description: TGWSubnet02 ID in the VPC
    Value: !Ref TGWSubnet02

  TGWSubnet03:
    Description: TGWSubnet03 ID in the VPC
    Value: !Ref TGWSubnet03
```

</p></details>

- 위 파일을 사용해 CloudFormation stack을 생성해보자.

  ```sh
  aws cloudformation deploy \
    --stack-name "planit-eks-vpc" \
    --template-file "EKSVPC3AZ.yml" \
    --capabilities CAPABILITY_NAMED_IAM
  ```

- 대략 5분 뒤, AWS Console의 CloudFormation에 접속하면 아래처럼 stack이 생성된 모습을 볼 수 있다.

  ![picture 7](/images/AWS_DEVOPS_EKS_3.png)

### Stack output 확인하기

- 위 화면에서 `Outputs`로 가보면, 아래의 리소스들이 생성된 것을 확인할 수 있다.

- 우리는 여기서 VPC id, Subnet id가 필요하다. 아래처럼 총 3개의 private subnet, 3개의 public subnet이 생성되었다.

  ![picture 8](/images/AWS_DEVOPS_EKS_4.png)

- VPC 구성은 완료되었지만, worker node들은 아직 생성되지 않았다. 이를 eksctl을 사용해 생성해보자.

---

## (3) - eksctl 기반 k8s 설정

- 위에서 생성한 VPC의 id, subnet id들은 이후에도 많이 사용되므로 아래 명령어를 실행해 환경 변수에 저장 및 별도 파일에 저장하도록 하자.

```sh
#VPC ID export
export vpc_ID=$(aws ec2 describe-vpcs --filters Name=tag:Name,Values=planit-eks-vpc | jq -r '.Vpcs[].VpcId')
echo $vpc_ID

#Subnet ID, CIDR, Subnet Name export
aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)'
echo $vpc_ID > vpc_subnet.txt
aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' >> vpc_subnet.txt
cat vpc_subnet.txt

# VPC, Subnet ID 환경변수 저장
export PublicSubnet01=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PublicSubnet01/{print $1}')
export PublicSubnet02=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PublicSubnet02/{print $1}')
export PublicSubnet03=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PublicSubnet03/{print $1}')
export PrivateSubnet01=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PrivateSubnet01/{print $1}')
export PrivateSubnet02=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PrivateSubnet02/{print $1}')
export PrivateSubnet03=$(aws ec2 describe-subnets --filter Name=vpc-id,Values=$vpc_ID | jq -r '.Subnets[]|.SubnetId+" "+.CidrBlock+" "+(.Tags[]|select(.Key=="Name").Value)' | awk '/planit-eks-vpc-PrivateSubnet03/{print $1}')
echo "export vpc_ID=${vpc_ID}" | tee -a ~/.bash_profile
echo "export PublicSubnet01=${PublicSubnet01}" | tee -a ~/.bash_profile
echo "export PublicSubnet02=${PublicSubnet02}" | tee -a ~/.bash_profile
echo "export PublicSubnet03=${PublicSubnet03}" | tee -a ~/.bash_profile
echo "export PrivateSubnet01=${PrivateSubnet01}" | tee -a ~/.bash_profile
echo "export PrivateSubnet02=${PrivateSubnet02}" | tee -a ~/.bash_profile
echo "export PrivateSubnet03=${PrivateSubnet03}" | tee -a ~/.bash_profile
source ~/.zshrc
```

- 위 명령어를 실행하면 vpc id, subnet id가 모두 환경 변수에 저정되며 `vpc_subnet.txt`에 저장된다.  
  아래는 `vpc_subnet.txt` 파일의 예시 모습이다.

```
vpc-0e88a2ed7a32c0336
subnet-02b5356084f4355cb 10.11.16.0/20 planit-eks-vpc-PublicSubnet02
subnet-0ea280f1567234a3b 10.11.253.0/24 planit-eks-vpc-TGWSubnet03
subnet-0a79d22a3acf610bf 10.11.32.0/20 planit-eks-vpc-PublicSubnet03
subnet-0c96f4c64e724524b 10.11.251.0/24 planit-eks-vpc-TGWSubnet01
subnet-0d5d255e8542cf405 10.11.64.0/20 planit-eks-vpc-PrivateSubnet02
subnet-0c3c37774542ac95c 10.11.252.0/24 planit-eks-vpc-TGWSubnet02
subnet-07f97eaa984b5ced2 10.11.0.0/20 planit-eks-vpc-PublicSubnet01
subnet-0ebb353a91c36ab17 10.11.48.0/20 planit-eks-vpc-PrivateSubnet01
subnet-0e3c3abe52dbe07e8 10.11.80.0/20 planit-eks-vpc-PrivateSubnet03
```

- 마지막으로 aws region을 환경 변수로 넣어두자.

  ```sh
  export AWS_REGION=ap-northeast-2
  ```

- 이전 단계에서 환경 변수로 설정했던 VPC id, subnet id, region, kms master arn은 eksctl을 통해 EKS cluster를 배포하는 데 사용된다.

### Cluster 생성

- Cluster를 생성하기 전, 몇 개의 추가적인 환경 변수를 설정하고 잘 나오는지 테스트해보자.

```sh
export ekscluster_name="planit-dev-eks"
export eks_version="1.21"
export instance_type="t3a.medium"
echo ${AWS_REGION}
echo ${eks_version}
echo ${PublicSubnet01}
echo ${PublicSubnet02}
echo ${PublicSubnet03}
echo ${PrivateSubnet01}
echo ${PrivateSubnet02}
echo ${PrivateSubnet03}
echo ${MASTER_ARN}
```

- [이 공식문서](https://docs.aws.amazon.com/eks/latest/userguide/fargate-profile.html)에 나와있는 것처럼 fargate는 private subnet에만 배정할 수 있다.

  ![picture 9](/images/AWS_DEVOPS_EKS_5.png)

- eksctl이 참조할 파일은 아래와 같다.

<details><summary>ekscluster-3az.yml</summary>

<p>

```yaml
# A simple example of ClusterConfig object:
---
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: ${ekscluster_name}
  region: ${AWS_REGION}
  version: "${eks_version}"

vpc:
  id: ${vpc_ID}
  subnets:
    private:
      PrivateSubnet01:
        az: ${AWS_REGION}a
        id: ${PrivateSubnet01}
      PrivateSubnet02:
        az: ${AWS_REGION}b
        id: ${PrivateSubnet02}
      PrivateSubnet03:
        az: ${AWS_REGION}c
        id: ${PrivateSubnet03}

secretsEncryption:
  keyARN: ${MASTER_ARN}

fargateProfiles:
  - name: fp-managed-ng-public-01
    selectors:
      - namespace: planit-dev
    subnets:
      - ${PrivateSubnet01}
      - ${PrivateSubnet02}
      - ${PrivateSubnet03}

  - name: fp-managed-ng-private-01
    selectors:
      - namespace: planit-dev
    subnets:
      - ${PrivateSubnet01}
      - ${PrivateSubnet02}
      - ${PrivateSubnet03}

cloudWatch:
  clusterLogging:
    enableTypes:
      ["api", "audit", "authenticator", "controllerManager", "scheduler"]
```

</p></details>

- 이제 eksctl을 통해 EKS cluster를 생성하자.

- 먼저 아래 명령을 실행한다.

```sh
cat << EOF > ekscluster-3az.yaml
# A simple example of ClusterConfig object:
---
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: ${ekscluster_name}
  region: ${AWS_REGION}
  version: "${eks_version}"

vpc:
  id: ${vpc_ID}
  subnets:
    private:
      PrivateSubnet01:
        az: ${AWS_REGION}a
        id: ${PrivateSubnet01}
      PrivateSubnet02:
        az: ${AWS_REGION}b
        id: ${PrivateSubnet02}
      PrivateSubnet03:
        az: ${AWS_REGION}c
        id: ${PrivateSubnet03}

secretsEncryption:
  keyARN: ${MASTER_ARN}

fargateProfiles:
  - name: fp-managed-ng-public-01
    selectors:
      - namespace: planit-dev
    subnets:
      - ${PrivateSubnet01}
      - ${PrivateSubnet02}
      - ${PrivateSubnet03}

  - name: fp-managed-ng-private-01
    selectors:
      - namespace: planit-dev
    subnets:
      - ${PrivateSubnet01}
      - ${PrivateSubnet02}
      - ${PrivateSubnet03}

cloudWatch:
  clusterLogging:
    enableTypes:
      ["api", "audit", "authenticator", "controllerManager", "scheduler"]

EOF
```

- 그리고 아래 명령어로 eks cluster를 생성한다.

```sh
eksctl create cluster --config-file=ekscluster-3az.yaml
```

### kubectl로 연결하기

- eksctl로 클러스터는 만들어졌지만, 아래의 에러가 마지막에 출력된다.

  ![picture 10](/images/AWS_DEVOPS_EKS_6.png)

- 일단 아래 명령어로 kubeconfig를 설정해주자.

```sh
aws eks update-kubeconfig --region ap-northeast-2 --name planit-dev-eks
```

- 그리고 kubectl 명령어를 실행해보면, 아래의 에러가 발생한다.

```sh
# kubectl get svc
error: exec plugin: invalid apiVersion "client.authentication.k8s.io/v1alpha1"
```

- 심지어 `kubectl version` 명령어를 수행해도 동일한 에러가 발생한다.

- 이 문제는 kubernetes의 버전과 kubectl의 버전이 불일치해서 발생하는데, 현재 우리는 kubenetes를 1.21로 가동했다.  
  따라서 이를 지원하는 kubectl 버전 1.22로 설치해 사용하도록 하자.

> kubectl 버전과 kubernetes 버전 관련 문서는 [여기](https://docs.aws.amazon.com/eks/latest/userguide/install-kubectl.html)에서 확인할 수 있다.

```sh
curl -o kubectl https://s3.us-west-2.amazonaws.com/amazon-eks/1.22.6/2022-03-09/bin/darwin/amd64/kubectl
chmod +x ./kubectl
mkdir -p $HOME/bin && cp ./kubectl $HOME/bin/kubectl && export PATH=$HOME/bin:$PATH
echo 'export PATH=$PATH:$HOME/bin' >> ~/.zshrc
kubectl version --short --client
kubectl version
```

- 이후 `kubectl get svc`를 수행하면, 아래의 내용이 출력된다.

```sh
kubectl get svc
# NAME         TYPE        CLUSTER-IP   EXTERNAL-IP   PORT(S)   AGE
# kubernetes   ClusterIP   172.20.0.1   <none>        443/TCP   43m
```

---
