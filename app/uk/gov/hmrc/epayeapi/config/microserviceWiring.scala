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

import play.api.Logger
import uk.gov.hmrc.auth.core.{PlayAuthConnector => CoreAuthConnector}
import uk.gov.hmrc.http.hooks.HttpHooks
import uk.gov.hmrc.http.{HttpPost, _}
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.{AuthConnector => PlayAuthConnector}
import uk.gov.hmrc.play.config.{AppName, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.play.microservice.config.LoadAuditingConfig

trait Hooks extends HttpHooks with HttpAuditing {
  override val hooks = Seq(AuditingHook)
  override lazy val auditConnector: AuditConnector = MicroserviceAuditConnector
}

trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete with Hooks with AppName
object WSHttp extends WSHttp with AppName with HttpAuditing

object MicroserviceAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig = LoadAuditingConfig(s"$env.auditing")
}

@Singleton
case class MicroserviceAuthConnector @Inject() (servicesConfig: ServicesConfig)
  extends PlayAuthConnector with WSHttp {

  Logger.info(s"Starting: ${getClass.getName}")

  override lazy val authBaseUrl: String = servicesConfig.baseUrl("auth")
}

@Singleton
case class ActualAuthConnector @Inject() (
  servicesConfig: ServicesConfig,
  http: HttpPost
)
  extends CoreAuthConnector {
  lazy val serviceUrl: String = servicesConfig.baseUrl("auth")
}
