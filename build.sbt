// See README.md for license details.

val majorChiselVersion = "6"
val minorChiselVersion = "2"
val chiselVersion = majorChiselVersion + "." + minorChiselVersion + ".0"

scalaVersion     := "2.13.12"
version          := majorChiselVersion + "." + minorChiselVersion + ".9"
organization     := "org.armadeus"

credentials += Credentials(
  "GitHub Package Registry",
  "maven.pkg.github.com",
  "_",
  System.getenv("GITHUB_TOKEN")
)

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

publishTo := Some("GitHub Martoni Apache Maven Packages" at "https://maven.pkg.github.com/Martoni/WbPlumbing")
publishMavenStyle := true
