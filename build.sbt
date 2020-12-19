val akkaV = "2.5.31"
val akkaHttpV = "10.1.12"
val reaktiveV = "0.12.1"
val grpcV = "1.32.1"

enablePlugins(AkkaGrpcPlugin)

libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-actor-typed" % akkaV,
    "com.typesafe.akka" %% "akka-remote" % akkaV, // for protobuf3 serialization
    "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpV, // JSON serialization
    "io.vavr" % "vavr" % "0.9.0",
    "com.opencsv" % "opencsv" % "5.1",
    "junit" % "junit" % "4.11" % "test",
    "org.assertj" % "assertj-core" % "3.2.0" % "test",
    "org.mockito" % "mockito-core" % "2.2.27" % "test",
    "info.solidsoft.mockito" % "mockito-java8" % "2.0.0" % "test",
    "com.novocode" % "junit-interface" % "0.11" % "test",
    "org.forgerock.cuppa" % "cuppa" % "1.3.1" % "test",
    "org.forgerock.cuppa" % "cuppa-junit" % "1.3.1" % "test"
  )

fork in run := true
cancelable in Global := true
akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Java)