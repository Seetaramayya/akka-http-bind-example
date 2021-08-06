name := "akka-http-bind-example"

version := "0.1"

scalaVersion := "2.11.12"

val akkaVersion = "2.4.20"
val akkaHttpVersion = "10.0.15"

// https://github.com/scanamo/scanamo
lazy val dynamoDB = "com.gu" % "scanamo_2.11" % "1.0.0-M8"
lazy val alpakkaDynamoDB = "com.lightbend.akka" %% "akka-stream-alpakka-dynamodb" % "1.0.2"
lazy val dynamoDBJavaSDK = "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.867"
lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
lazy val sprayJson = "io.spray" %% "spray-json" % "1.3.5"
lazy val akkaHttpSpray = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion

libraryDependencies ++= Seq(akkaHttpSpray, akkaHttp, akkaStream, alpakkaDynamoDB, dynamoDBJavaSDK, sprayJson)
