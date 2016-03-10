//import sbtassembly.AssemblyKeys._

name := "pio_assignment"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.apache.spark" % "spark-core_2.11" % "1.6.0" % "provided",
  "org.apache.spark" % "spark-mllib_2.11" % "1.6.0" % "provided",
  "org.apache.spark" % "spark-sql_2.11" % "1.6.0" % "provided",
  "joda-time" % "joda-time" % "2.9.2",
  "org.joda" % "joda-convert" % "1.8",

  // wisp for plotting
  "com.quantifind" %% "wisp" % "0.0.4"
)

scalacOptions in (Compile, doc) ++= Seq(
  "-skip-packages",
  Seq(
    "akka",
    "io",
    "sbt").mkString(":"))