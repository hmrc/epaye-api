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

package uk.gov.hmrc.epayeapi.connectors

import javax.inject.{Inject, Singleton}

import play.api.Logger
import uk.gov.hmrc.api.domain.Registration
import uk.gov.hmrc.epayeapi.config.{AppContext, WSHttp}
import uk.gov.hmrc.http.{HeaderCarrier, HttpPost}

import scala.concurrent.{ExecutionContext, Future}

trait ServiceLocatorConnectorTrait {
  def appName: String
  def appUrl: String
  def serviceUrl: String
  def handlerOK: () => Unit
  def handlerError: Throwable => Unit
  def metadata: Option[Map[String, String]]
  def http: HttpPost
  implicit def executionContext: ExecutionContext
  def register(implicit hc: HeaderCarrier): Future[Boolean] = {
    val registration = Registration(appName, appUrl, metadata)
    http.POST(s"$serviceUrl/registration", registration, Seq("Content-Type" -> "application/json")) map {
      _ =>
        handlerOK()
        true
    } recover {
      case e: Throwable =>
        handlerError(e)
        false
    }
  }
}

@Singleton
case class ServiceLocatorConnector @Inject() (
  context: AppContext,
  http: WSHttp,
  implicit val executionContext: ExecutionContext
) extends ServiceLocatorConnectorTrait {
  val appName: String = context.appName
  val appUrl: String = context.appUrl
  val serviceUrl: String = context.serviceLocatorUrl
  val handlerOK: () => Unit = () => Logger.info("Service is registered on the service locator")
  val handlerError: Throwable => Unit = { error =>
    Logger.error(s"Service could not register on the service locator", error)
  }
  val metadata: Option[Map[String, String]] = Some(Map("third-party-api" -> "true"))
}


