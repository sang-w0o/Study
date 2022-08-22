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
