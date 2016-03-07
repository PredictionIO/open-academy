//import sbtassembly.AssemblyKeys._

name := "data_analysis"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % "1.6.0" % "provided",
  "org.apache.spark" % "spark-mllib_2.11" % "1.6.0" % "provided",
  "org.apache.spark" % "spark-sql_2.11" % "1.6.0" % "provided",
  "joda-time" % "joda-time" % "2.9.2",
  "com.databricks" % "spark-csv_2.11" % "1.4.0")


scalacOptions in (Compile, doc) ++= Seq(
  "-skip-packages",
  Seq(
    "akka",
    "io",
    "sbt").mkString(":"))
