name := "PathRouter"

version := "0.0.0"

scalaVersion := "2.10.0"

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies += "com.iinteractive" % "scala-test-more_2.10" % "0.01" % "test"

testFrameworks += new TestFramework("com.iinteractive.test.sbt.Framework")

libraryDependencies += "io.netty" % "netty" % "3.5.5.Final"