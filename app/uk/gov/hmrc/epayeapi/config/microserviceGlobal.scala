/*
 * Copyright 2018 HM Revenue & Customs
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

package uk.gov.hmrc.epayeapi.config

import javax.inject.{Inject, Singleton}

import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._
import play.api.inject.ApplicationLifecycle
import play.api.{Application, Configuration, Logger}
import uk.gov.hmrc.epayeapi.connectors.ServiceLocatorConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config.{ControllerConfig, RunMode}
import uk.gov.hmrc.play.microservice.bootstrap.DefaultMicroserviceGlobal
import uk.gov.hmrc.play.microservice.filters.{AuditFilter, LoggingFilter, MicroserviceFilterSupport}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
case class ControllerConfiguration @Inject() (config: Configuration)
  extends ControllerConfig {
  Logger.info(s"Starting: ${getClass.getName}")
  lazy val controllerConfigs: Config = config.underlying.as[Config]("controllers")
}

@Singleton
case class AuthParamsControllerConfiguration @Inject() (config: ControllerConfiguration)
  extends AuthParamsControllerConfig {
  Logger.info(s"Starting: ${getClass.getName}")
  lazy val controllerConfigs: Config = config.controllerConfigs
}

@Singleton
case class MicroserviceAuditFilter @Inject() (
  context: AppContext,
  config: ControllerConfiguration,
  auditConnector: AuditConnector
)
  extends AuditFilter
  with MicroserviceFilterSupport {
  Logger.info(s"Starting: ${getClass.getName}")
  val appName: String = context.appName
  def controllerNeedsAuditing(controllerName: String): Boolean =
    config.paramsForController(controllerName).needsAuditing
}

@Singleton
case class MicroserviceLoggingFilter @Inject() (
  config: ControllerConfiguration
)
  extends LoggingFilter
  with MicroserviceFilterSupport {
  Logger.info(s"Starting: ${getClass.getName}")
  def controllerNeedsLogging(controllerName: String): Boolean =
    config.paramsForController(controllerName).needsLogging
}

@Singleton
case class MicroserviceAuthFilter @Inject() (
  config: ControllerConfiguration,
  authParamsConfig: AuthParamsControllerConfiguration,
  authConnector: MicroserviceAuthConnector
)
  extends AuthorisationFilter
  with MicroserviceFilterSupport {
  Logger.info(s"Starting: ${getClass.getName}")
  def controllerNeedsAuth(controllerName: String): Boolean =
    config.paramsForController(controllerName).needsAuth
}

@Singleton
case class MicroserviceGlobal @Inject() (
  mode: RunMode,
  auditConnector: AuditConnector,
  authFilterPure: MicroserviceAuthFilter,
  loggingFilter: MicroserviceLoggingFilter,
  microserviceAuditFilter: MicroserviceAuditFilter,
  context: AppContext,
  config: Configuration
)
  extends DefaultMicroserviceGlobal
  with MicroserviceFilterSupport {
  Logger.info(s"Starting: ${getClass.getName}")
  // Overriding values so they don't use Play.current and crash our app on
  // startup
  override lazy val appName: String = context.appName
  override lazy val loggerDateFormat: Option[String] = config.getString("logger.json.dateformat")
  val authFilter = Some(authFilterPure)
  def microserviceMetricsConfig(implicit app: Application): Option[Configuration] =
    app.configuration.getConfig(s"microservice.metrics")
}

trait Startup {
  def start(): Unit
}

case class AppStartup @Inject() (
  controllerConfiguration: ControllerConfiguration,
  authParamsControllerConfiguration: AuthParamsControllerConfiguration,
  microserviceAuditFilter: MicroserviceAuditFilter,
  microserviceLoggingFilter: MicroserviceLoggingFilter,
  microserviceAuthFilter: MicroserviceAuthFilter,
  microserviceGlobal: MicroserviceGlobal,
  serviceLocator: ServiceLocatorConnector,
  app: Application,
  lifecycle: ApplicationLifecycle,
  implicit val ec: ExecutionContext
) extends Startup {
  lifecycle.addStopHook(() => Future.successful(microserviceGlobal.onStop(app)))
  microserviceGlobal.onStart(app)
  def start(): Unit = {
    // Makes an HTTP request that requires Play.current. While starting up
    // Play.current is not available so we defer a call to .start into a
    // controller.
    serviceLocator.register(HeaderCarrier()).foreach({
      case true => Logger.info("Registered with service locator")
      case false => Logger.error("Failed registering with service locator")
    })
  }
}
