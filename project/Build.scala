import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "CNCProductionManagement"
  val appVersion      = "0.1"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
    "org.scalatest" %% "scalatest" % "1.9.1" % "test",
    "com.typesafe.slick" %% "slick" % "1.0.0",
    "org.scalaz" %% "scalaz-core" % "7.0.0-M8",
    "org.slf4j" % "slf4j-nop" % "1.6.6",
    "org.xerial" % "sqlite-jdbc" % "3.7.2",
    "postgresql" % "postgresql" % "9.1-901.jdbc4",
    "com.github.nscala-time" %% "nscala-time" % "0.2.0"
  )


  val main = play.Project(appName, appVersion, appDependencies).settings(
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    scalaVersion:="2.10.0"
  )

}
