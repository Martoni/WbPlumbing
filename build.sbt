// See README.md for license details.

val majorChiselVersion = "6"
val minorChiselVersion = "2"
val chiselVersion = majorChiselVersion + "." + minorChiselVersion + ".0"

scalaVersion     := "2.13.8"
version          := majorChiselVersion + "." + minorChiselVersion + ".4"
organization     := "org.armadeus"

lazy val root = (project in file("."))
  .settings(
    name := "wbplumbing",
    libraryDependencies ++= Seq(
      "org.chipsalliance" %% "chisel" % chiselVersion,
      "org.scalatest" %% "scalatest" % "3.2.16" % "test",
    ),
    scalacOptions ++= Seq(
      "-language:reflectiveCalls",
      "-deprecation",
      "-feature",
      "-Xcheckinit",
      "-Ymacro-annotations",
    ),
    addCompilerPlugin("org.chipsalliance" % "chisel-plugin" % chiselVersion cross CrossVersion.full),

  )
