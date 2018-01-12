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

package unit

import akka.stream.Materializer
import akka.util.ByteString
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{Matchers, OptionValues, fixture}
import org.scalatestplus.play.{MixedFixtures, WsScalaTestClient}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.streams.Accumulator
import play.api.mvc.Result
import uk.gov.hmrc.auth.core.AuthConnector

import scala.concurrent.Future
import scala.reflect.ClassTag

abstract class AppSpec
  extends fixture.WordSpec
  with Matchers
  with MockitoSugar
  with ScalaFutures
  with OptionValues
  with MixedFixtures
  with Eventually
  with IntegrationPatience
  with WsScalaTestClient {
  def inject[A: ClassTag](implicit a: Application): A = a.injector.instanceOf[A]

  def build(auth: AuthConnector): Application =
    GuiceApplicationBuilder()
      .overrides(bind(classOf[AuthConnector]).toInstance(auth))
      .build

  case class Builder(builder: GuiceApplicationBuilder) {
    def update(fn: GuiceApplicationBuilder => GuiceApplicationBuilder): Builder = copy(fn(builder))
    def withAuth(connector: AuthConnector): Builder =
      update(_.overrides(bind(classOf[AuthConnector]).toInstance(connector)))
    def withConfig(confs: (String, Any)*): Builder =
      update(_.configure(confs: _*))
    def build: Application = builder.build()

  }

  def builder: Builder = Builder(GuiceApplicationBuilder())

  // Actions return Accumulators, but the helpers expect Future[Results]
  implicit def accumulatorToFuture(acc: Accumulator[ByteString, Result])(implicit mat: Materializer): Future[Result] =
    acc.run()

  // Turning Accumulators into Futures requires materializers
  implicit def materializer(implicit a: Application): Materializer = inject[Materializer]

  def apiBaseUrl(implicit a: Application): String =
    a.configuration.underlying.getString("api.baseUrl")
}
