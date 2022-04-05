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

## Network Load Balancer(NLB)

- Ultra-high performance while maintaining very low latency.
- Operates at connection level, routing traffics to targets within VPC.
- Handles millions of request per second.

## Classic Load Balancer

- Used for applications that were built in the classic EC2 environment.
- Operates at both request and connection level.

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
