<h1>Get Started</h1>

* Problems before Docker containers
  * Each Individual Machine or VM.
    * Install OS, Upgrade and Path it. Install dependencies for the applicatino that will run on this insance.
  * Maintain the machine.
    * Keep up-to-date with patches, security update, and upgrades.
  * Install application code.

  * Extremely Error Prone
    * Servers were missed.
    * Led to intermittent errors.
    * Multiple layers to troubleshoot.
    * Complete nightmare to troubleshoot.

* Docker's solution
  * What if we could bundle all our application codes, support binaries, and configuration together   
    and only do that ONCE? --> Image
    
* After Docker Containers were made.
  * Deploy multiple images on each server.
  * If one image goes done, we just have to deploy a new image, which is all copies of one another.

* Docker's point
  * Build Image : Consistently package everything your application needs to run.
  * Ship Image : Easily ship these images to runtimes in the cloud or on your local developer machine.
  * Run Image : Easily and consistently execute your applications.
  * CI/CD : Consistently test and deploy your code to different environments.
  * Different Versions : Easily run different version of your software or vendor software without installation.
  * Roll Forward : When a defect is found, no need to patch or update the application.   
    Just ship a new image!
<hr/>

<h2>Dockerfile</h2>

* Dockerfile is used to build an image of your application code.   
  It is just a list of command that consents to docker engine, executing each one in term.

* Example of dockerfile.
```Dockerfile
FROM node:12.16.3

WORKDIR /code

ENV PORT 80

COPY package.json /code/package.json

RUN npm install

COPY . ./code

CMD ["node", "src/server.js"]
```

* `FROM`에 지정하는 값은 Base Image로, Docker Hub에 있는 Verified된 image이다.
  * Base image has everything you need in it.
  * 위에서 사용한 예시는 Node Application에 대한 예시인데, `node:12.16.3`은 Node Base Image이다.
  * So we tell docker to start with `node:12.16.3` image, and after that we are going to build on top of it.

* `WORKDIR` : This command tells docker to use given value as a working directory.
* `ENV` : This commands set environment variable.
* `COPY` : Copies file `arg1` to `arg2`. (`arg1` : local, `arg2` : Directory in image)
* `RUN` : Command that docker will execute.
* `CMD` : Default command that docker will run when the image is executed.
<hr/>

<h2>Building docker image using Docker</h2>

```
docker build [OPTIONS] PATH | URL | -
```

* `docker build` command builds an image to be used in docker container.
```
docker build --tag hello-world .
```

* `--tag` : Sets the name for image file to be created.(Above example : hello-world)
* `.` : Location where newly built docker image will be stored.

* `docker images` : Shows the list of docker images in local directory.
* `docker ps -a` : Shows processes running on docker.
* `docker start [imagename]` : Starts container using image file "imagename".
* `docker stop [imagename]` : Stops container using image file "imagename".
* `docker rm [imagename]` : Removes image file called "imagename".
<hr/>

<h2>Running image file on docker container</h2>

```
docker run [OPTIONS] IMAGE [COMMAND] [ARG...]
```

* `docker run` command runs a built docker image on a container.
```
docker run hello-world
```

* After above command is exeuted, nothing will show up.   
  That's because __containers run in isolation.__ A container is basically a process which is isolated from   
  rest of the system. It has its own file system, own network..etc.   
  So we have to __map out ports to access the application running in a container in local environment.__   

```
docker run -p 8080:80 --name hello -d hello-world
```

* Runs a docker container using local's 8080 port to container's 80 port, naming the container "hello",   
  making it run in background(detached mode), and using image file called "hello-world".

* `docker stop [containername]` stops the container running.

```
docker stop hello
```

* `docker logs [containername]` shows logs of container named "containername".
  * `-f` option allows you to follow the log.
<hr/>

<h2>Sharing image you built on Docker Hub</h2>

* Docker Hub is where all the base images live, which is a repository for images.
* `docker push` pushes local docker image file to Docker Hub.
```
docker push imageName
```

* `docker pull` pulls image from Docker Hub to local machine.
<hr/>

<h2>Docker Compose</h2>

* Docker compose composes alot of files together. (`docker-compose.yml`)
```yml
version: '2'

services:
    web:
        build:  
            context: .
            dockerfile: Dockerfile
        container_name: web
        ports:
            - "8080:80"
```

* To run a docker compose file, use command `docker-compose`.
```
docker-compose up -d
```

* The above command starts a new container in detached mode(`-d`).   

* `docker-compose` looks up in your current directory, for example "docker-hello-world", and prepends   
  the name of your currrent directory, appends `container_name` in `docker-compose.yml`, and starts   
  docker container. So in this case, image file named "docker-hello-world_web" is created.   
  And the newly created container using this image file is named "web".

* `docker-compose stop` stops the service and removes it.

* Let's say that we need another service, for example a database.   
  Let's put information about it in `docker-compose.yml`.
```yml
version: '2'

services:
    web:
        build:  
            context: .
            dockerfile: Dockerfile
        container_name: web
        ports:
            - "8080:80"
    
    db:
        image: mongo:3.6.1
        container_name: db
        volumes:
            - mongodb:/data/db
            - mongodb_config:/data/configdb
        ports:
            - 27017:27017
        command: mongod
    
    volumes:
        mongodb:
        mongodb_config:
```

* `docker-compose up -d`, and then docker will download mongoDB image for you.   
  And we can see two images, "docker-hello-world_web" and "mongo:3.6.1" running   
  running on "web" and "db" containers, respectively.

