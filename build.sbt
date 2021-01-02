import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

mainClass in (Compile, run) := Some("KDTree.Main")

lazy val root = (project in file("."))
  .settings(
    name := "k_d_tree",
  )

