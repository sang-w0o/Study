# Elastic Load Balancing

- ELB(Elastic Load Balancer): **Manage and control the flow of inbound requests destined to a group of targets**  
  by distributing these requests evenly across the targeted resource group.

  - Targets of ELB could resign in different AZ.
  - ELB itself is comprised of multiple instances, fully managed by AWS.
  - ELB is fully managed by AWS.
    - Automatically scales up/down by traffic.

## Application Load Balancer(ALB)

- Flexible feature set for your web applications running the HTTP or HTTPS protocols.
- Operates at request level.
- Advanced routing, TLS termination and visibility features targeted at application architectures.

- ALB operates at layer 7, the application layer.

  - The application layer serves as the interface for users and application processes to access network services.
  - Examples of the application processes: HTTP, HTTPS, STMP, NFS, etc.

- ALB always has cross-zone load balancing enabled.

## Network Load Balancer(NLB)

- Ultra-high performance while maintaining very low latency.
- Operates at connection level, routing traffics to targets within VPC.
- Handles millions of request per second.

- NLB operates at layer 4, the transport layer.

  - Enables you to balance requests purely based on TCP/UDP protocol.

- Cross-zone load balancing on NLB can be enabled or disabled.

## Classic Load Balancer

- Used for applications that were built in the classic EC2 environment.
  - It is best practice to use ALB over Classic Load Balancer unless you have an existing application  
    running in the EC2-Classic network.
- Operates at both request and connection level.

---

## Components of ELB

### Listeners

- For every load balancer, you must configure at least one listener.
- The listener defines how your inbound connections are routed to your target groups based on ports and protocols  
  set as conditions.

### Target Groups

- A target group is a group of your resources that you want your ELB to route requests to.
- You can configure ELB with a number of different target groups, each associated with a different listener configuration  
  and associated rules.

### Rules

- Rules are associated to each listener that you have configured within your ELB.
- They help to define how an incoming request gets routed to which target group.

![picture 1](/images/SAA_ELB_1.png)

### Health Checks

- A health check that is performed against the resources difined within the target group.
- These health checks allow the ELB to contact each target using a specific protocol to receive a response.

### Internet-Facing ELB

- The nodes of the ELB are accessible via the the internet and so have a public DNS name that can be resolved  
  to its public IP address, in addition to internal IP address.
- This allows the ELB to serve incoming requests from the internet before distributing and routing the traffic  
  to your target groups.

### Internal ELB

- An Internal ELB only has internal IP address, which means that it can only serve requests that originate from  
  within your VPC itself.

### ELB Nodes

- For each AZ selected, and ELB node will be placed within that AZ.
- You need to ensure that you have an ELB node associated to any AZs for which you want to route traffic to.
- The nodes are used by the ELB to distribute traffic to your target groups.

### Cross-Zone Load Balancing

- When cross-zone load balancing is disabled, each ELB in its associated AZ will distribute its traffic with the  
  targets within that AZ only.
- With cross-zone load balancing enabled, the ELBs will distribute all incoming traffic evenly between all targets.

---

## Using ELB and Auto Scaling together

- It is easy to associate your EC2 Auto Scaling Group to an ELB

  - The ELB allows you to dynamically manage load across your resources based on target groups and rules.
  - EC2 Auto Scaling allows you to elastically scale those target groups based upon the demand put upon  
    your infrastructure.

- Combining an ELB and Auto Scaling helps you to manage and automatically scale your EC2 compute resources  
  both in and out.

- When you attach an ELB to an Auto Scaling Group, the ELB will automatically detect the instances  
  and start to distribute all traffic to the resources in the Auto Scaling Group.

  - To associate an ALB or NLB, you must associate the Auto Scaling Group with the ELB Target Group.
  - For the Classic Load Balancer, the EC2 fleet will be registered directly with the load balancer.

---

## Launch Configurations

- Launch configuration is a template that the Auto Scaling group uses to launch Amazon EC2 instances.  
  Launch configuration includes information such as AMI ID to use for launching the EC2 instance,  
  the instance type, key pairs, security groups and blocked device mappings among other configuration settings.  
  When you create your auto scaling group, you must associate it with a laungh configuration and you can attach  
  only one launch configuration to an auto scaling group at any time.

- Launch configurations cannot not be modified(immutable).  
  So if you want to change the launch configuration of your Auto Scaling group, you have to first create a new  
  launch configuration and then update your auto scaling group by attaching the new one.

- When you attach a new launch configuration to your auto scaling group, any new instances are launched using a  
  new configuration parameters. **Existing instances are not affected.**

## Launch Templates

- You can have multiple versions of launch templates saved and disassociate one version from an auto scaling  
  group and replace it with another without having to delete any existing templates or stop or restart your  
  auto scaling group.

---
