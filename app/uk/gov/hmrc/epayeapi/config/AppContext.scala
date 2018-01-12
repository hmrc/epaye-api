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

import play.api.{Configuration, Logger}
import uk.gov.hmrc.play.config.inject.DefaultServicesConfig

import scala.util.Try

@Singleton
case class AppContext @Inject() (config: DefaultServicesConfig) {
  val current: Configuration = config.runModeConfiguration
  val env = config.environment.mode

  val appName: String = current.getString("appName").getOrElse(throw new RuntimeException("appName is not configured"))
  val appUrl: String = current.getString("appUrl").getOrElse(throw new RuntimeException("appUrl is not configured"))
  val serviceLocatorUrl: String = config.baseUrl("service-locator")
  val apiContext: String = current.getString("api.context").getOrElse(throw new RuntimeException(s"Missing Key 'api.context' in environment $env."))
  val apiStatus: String = current.getString("api.status").getOrElse(throw new RuntimeException(s"Missing Key 'api.status' in environment $env."))
  val apiBaseUrl : String = current.getString("api.baseUrl").getOrElse(throw new RuntimeException(s"Missing Key 'api.baseUrl' in environment $env."))
  val useSandboxConnectors: Boolean =
    Try(current.getString("useSandboxConnectors").getOrElse("false").toBoolean)
      .getOrElse(false)
  val whitelistedApplications: Seq[String] =
    current.getString("whitelistedApplications").getOrElse("")
      .split(",").filter(_.nonEmpty).map(_.trim)

  Logger.info(
    s"AppContext startup: " +
      s"env=$env " +
      s"appName=$appName " +
      s"appUrl=$appUrl " +
      s"serviceLocatorUrl=$serviceLocatorUrl " +
      s"apiContext=$apiContext " +
      s"apiStatus=$apiStatus " +
      s"useSandboxConnectors=$useSandboxConnectors " +
      s"whitelistedApplications=$whitelistedApplications"
  )
}
