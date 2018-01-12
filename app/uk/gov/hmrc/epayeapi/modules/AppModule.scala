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

package uk.gov.hmrc.epayeapi.modules

import javax.inject.Singleton

import com.google.inject.{AbstractModule, Provides}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.epayeapi.config._
import uk.gov.hmrc.epayeapi.connectors.EpayeApiConfig
import uk.gov.hmrc.epayeapi.connectors.stub.SandboxAuthConnector
import uk.gov.hmrc.http.HttpPost
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.microservice.connectors.{AuthConnector => PlayAuthConnector}
import uk.gov.hmrc.play.config.RunMode
import uk.gov.hmrc.play.config.ServicesConfig

class AppModule() extends AbstractModule {
  def configure(): Unit = {
    bind(classOf[PlayAuthConnector]).to(classOf[MicroserviceAuthConnector]).asEagerSingleton()
//    bind(classOf[WSHttp]).to(classOf[WSHttp]).asEagerSingleton()
    bind(classOf[Startup]).to(classOf[AppStartup]).asEagerSingleton()
  }

  @Provides
  @Singleton
  def provideServicesConfig: ServicesConfig = {
    new ServicesConfig {}
  }

  @Provides
  @Singleton
  def provideRunMode: RunMode = {
    MicroserviceAuditConnector
  }

  @Provides
  @Singleton
  def provideAuditConnector: AuditConnector = {
    MicroserviceAuditConnector
  }

  @Provides
  @Singleton
  def provideHttpPost: HttpPost = {
    WSHttp
  }

  @Provides
  @Singleton
  def provideWSHttp: WSHttp = {
    WSHttp
  }

  @Provides
  @Singleton
  def provideEpayeApiConfig(context: AppContext): EpayeApiConfig = {
    EpayeApiConfig(
      context.config.baseUrl("epaye"),
      context.apiBaseUrl
    )
  }

  @Provides
  @Singleton
  def provideAuthConnector(context: AppContext, servicesConfig: ServicesConfig, http: WSHttp): AuthConnector = {
    if (context.useSandboxConnectors) {
      SandboxAuthConnector
    }
    else {
      ActualAuthConnector(servicesConfig, http)
    }
  }

}
