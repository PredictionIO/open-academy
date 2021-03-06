//import sbtassembly.AssemblyKeys._

name := "fbSpark"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.10" % "1.6.0" % "provided",
  "org.apache.spark" % "spark-mllib_2.10" % "1.6.0" % "provided",
  "org.apache.spark" % "spark-sql_2.10" % "1.6.0" % "provided",
  "joda-time" % "joda-time" % "2.9.2")


scalacOptions in (Compile, doc) ++= Seq(
  "-skip-packages",
  Seq(
    "akka",
    "io",
    "sbt").mkString(":"))
