name := "NewMovieServer"

version := "0.1"

scalaVersion := "2.13.8"

libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.17"

libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.2.6"

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.17"

libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.6"

libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.2.6"// % Test

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" //% Test

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.17" //% Test
libraryDependencies +="com.pauldijou" %% "jwt-spray-json" % "5.0.0"
libraryDependencies += "org.postgresql" % "postgresql" % "42.3.1"
