/*
 * Copyright 2017 Daniel Urban and contributors listed in AUTHORS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

lazy val proto = project
  .settings(name := "example-proto")
  .settings(commonSettings)
  .settings(
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch),
    mimaPreviousArtifacts := Set(organization.value %% name.value % "0.1.0"),
    sealsSchemaPackages += "com.example.proto"
  )

lazy val server = project
  .settings(name := "example-server")
  .settings(commonSettings)
  .dependsOn(proto)
  .settings(libraryDependencies ++= Seq(
    "io.sigs" %% "seals-scodec" % sealsVersion,
    "co.fs2" %% "fs2-io" % "0.10.0-M6"
  ))

lazy val example = project.in(file("."))
  .settings(name := "example")
  .settings(commonSettings)
  .aggregate(proto, server)

lazy val commonSettings = Seq[Setting[_]](
  organization := "com.example",
  version := "0.2.0-SNAPSHOT",
  scalaVersion := "2.11.11",
  libraryDependencies ++= Seq(
    "io.sigs" %% "seals-core" % sealsVersion,
    "org.scalatest" %% "scalatest" % "3.0.2" % Test
  )
)

lazy val sealsVersion = "0.1.0-SNAPSHOT" // TODO: don't hardcode version
