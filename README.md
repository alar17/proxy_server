# proxy_server
A second part of the duo which delivers prime numbers 

### 1) Connecting to the prime number server

In order to connect to the prime number server, we needed to import the
[protobuf](https://github.com/alar17/proxy_server/blob/main/src/main/protobuf/rpc.proto) definitions of the data model and the service and then
call the [prime numbers client](https://github.com/alar17/proxy_server/blob/368c832aad48871585b94fa1d241af995fdbdd11/src/main/java/com/proxy/server/primenumbersserver/PrimeNumbersProtocol.java#L33).

### 2) Running the server locally

In order to run the server locally, we need to install sbt and run the following commands:

* sbt: Runs sbt
* clean: Cleans the project (We don't need to do this every time)
* eclipse: Creates a java project (We need to do it only once)
* compile: Compiles the project
* test: Runs the tests of the project to make sure that everything is fine (Not needed for just running)
* run: Runs the server

### 3) REST API

#### 3.1 Status API
After running the server, you can call `http://localhost:8010/status`. You should be able to see the message `Server is running`. 

#### 3.2 Prime Numbers API
`http://localhost:8010/prime/[NUMBER]` is the path. NUMBER should be an integer between 2 and 2,147,483,647.
Since I have utilized asynchronous streaming architecture, you should be able to see the stream of numbers immediately, even if you call the API with a very large number.

### Disclaimer

The main focus of the project was to solve the problem using streaming, asynchronous scalable actor model and gRPC protocol.
There solution is stateless, hence no database is involved. Using statefull actors can potentially increase the performance based on the usage.
The project has been done in a limited time frame. There are some room for improvement and make the code a production quality code. Some future work includes, adding monitoring, better test coverage and adding authorization between servers.

