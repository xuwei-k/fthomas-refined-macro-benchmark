val common = Def.settings(
  scalaVersion := "2.13.14",
  libraryDependencies += "eu.timepit" %% "refined" % "0.11.2",
  scalacOptions ++= Seq("-feature", "-deprecation"),
)

def genSource(i: String): Def.SettingsDefinition = Def.settings(
  scalacOptions ++= Seq("-Yprofile-trace", s"${name.value}.json"),
  Compile / sourceGenerators += task {
    val dir = (Compile / sourceManaged).value
    val f = dir / "Main.scala"
    val body = (1 to 10000).map { n =>
      s"def x${n}: NonNegInt = $n"
    }.mkString("\n", "\n", "\n")
    val src =
      s"""|package example
          |
          |import ${i}
          |import eu.timepit.refined.types.numeric.NonNegInt
          |
          |object Main {
          |$body
          |}
          |""".stripMargin
    IO.write(f, src)
    Seq(f)
  }
)

val a1 = project.settings(common)

val a2 = project
  .settings(
    common,
    genSource("eu.timepit.refined.auto._"),
  )

val a3 = project
  .settings(
    common,
    genSource("example.MyRefinedAuto._"),
  )
  .dependsOn(a1)
