import sbt._

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val apiPlatformlibVersion = "1.3.0"
  private val domainVersion = "5.1.0"
  private val hmrcTestVersion = "3.0.0"
  private val logbackJsonLoggerVersion = "3.1.0"
  private val metricsGraphiteVersion = "3.0.2"
  private val microserviceBootstrapVersion = "6.18.0"
  private val pegdownVersion = "1.6.0"
  private val playConfigVersion = "4.3.0"
  private val playHealthVersion = "2.1.0"
  private val playUrlBindersVersion = "2.1.0"
  private val scalaTestVersion = "3.0.0"
  private val authClientVersion = "2.5.0"

  val compile = Seq(
    ws,
    "com.codahale.metrics" % "metrics-graphite" % metricsGraphiteVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "auth-client" % authClientVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-hmrc-api" % apiPlatformlibVersion,
    "uk.gov.hmrc" %% "play-url-binders" % playUrlBindersVersion
  )

  object Test {
    private val scope = "test"
    def apply(): Seq[ModuleID] = Seq(
      "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
      "org.pegdown" % "pegdown" % pegdownVersion % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.mockito" % "mockito-core" % "1.9.0" % scope,
      "com.github.tomakehurst" % "wiremock" % "1.57" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
      "com.github.java-json-tools" % "json-schema-validator" % "2.2.8" % scope
    )
  }

  object IntegrationTest {
    private val scope = "it"
    def apply(): Seq[ModuleID] = Seq(
      "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
      "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
      "org.pegdown" % "pegdown" % pegdownVersion % scope,
      "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
      "org.mockito" % "mockito-core" % "1.9.0" % scope,
      "org.scalaj" %% "scalaj-http" % "1.1.5" % scope,
      "com.github.tomakehurst" % "wiremock" % "1.57" % scope,
      "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % scope,
      "info.cukes" %% "cucumber-scala" % "1.2.4" % scope,
      "info.cukes" % "cucumber-junit" % "1.2.4" % scope,
      "com.github.java-json-tools" % "json-schema-validator" % "2.2.8" % scope
    )
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}

object MicroServiceBuild extends Build with MicroService {
  val appName = "epaye-api"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}
