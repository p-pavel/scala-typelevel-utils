Global / onChangedBuildSource := ReloadOnSourceChanges
ThisBuild / organization      := "com.perikov"
ThisBuild / scalaVersion      := "3.3.1"
ThisBuild / publishTo         := sonatypePublishToBundle.value
ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-explaintypes",
  "-explain",
  "-feature",
  "-source:future-migration",
  "-Ykind-projector",
  "-Yexplicit-nulls",
  "-language:stricEquality",
  "-Wunused:all",
  "-rewrite",
  "-java-output-version",
  "8"
)
ThisBuild / version           := "1.0.0"
ThisBuild / developers        := List(
  Developer(
    "p-pavel",
    "Pavel Perikov",
    "pavel@perikov.consulting",
    url("https://perikov.com")
  )
)
ThisBuild / versionScheme     := Some("semver-spec")
ThisBuild / licenses          := Seq(
  (
    "BSD3",
    url(
      "https://raw.githubusercontent.com/p-pavel/scala-typelevel-utils/main/LICENSE"
    )
  )
)
ThisBuild / homepage          := Some(
  url("https://github.com/p-pavel/scala-typelevel-utils")
)
ThisBuild / scmInfo           := Some(
  librarymanagement.ScmInfo(
    url("https://github.com/p-pavel/scala-typelevel-utils"),
    "git@github.com:p-pavel/scala-typelevel-utils.git"
  )
)
ThisBuild / startYear         := Some(2024)

lazy val api =
  project
    .in(file("src"))
    .enablePlugins(SbtOsgi)
    .settings(
      Compile / scalaSource := baseDirectory.value,
      Compile / doc / scalacOptions ++= Seq("-siteroot", "docs")
    )
    .settings(
      version           := "1.1.0-SNAPSHOT",
      name              := "Typelevel Utils",
      publishMavenStyle := true,
      moduleName        := "typelevel",
      description       := "Typelevel utilities for Scala 3"
    )
    .settings(
      OsgiKeys.exportPackage      := Seq("com.perikov.typelevel;version=${Bundle-Version}"),
      OsgiKeys.importPackage      := Seq(
        "scala.quoted.*;version=\"(3.3.1,4]\"",
        "*"
      ),
      OsgiKeys.privatePackage     := Seq("com.perikov.typelevel.compiletime"),
      OsgiKeys.bundleSymbolicName := "com.perikov.typelevel",
      OsgiKeys.additionalHeaders  := Map(
        "Bundle-Vendor"      -> "com.perikov",
        "Bundle-Description" -> "Typelevel utilities for Scala 3",
        "Bundle-DocURL"      -> "https://github.com/p-pavel/scala-typelevel-utils",
        "Require-Capability" -> "osgi.ee;filter:=\"(&(osgi.ee=JavaSE)(version=8.0))\""
      ),
      OsgiKeys.bundleVersion      := version.value
    )

lazy val tests =
  project
    .in(file("tests"))
    .dependsOn(api)
    .settings(
      Compile / scalaSource := baseDirectory.value)

lazy val build =
  project
    .in(file("."))
    .dependsOn(api)
    .settings(
    )
