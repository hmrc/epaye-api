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

package contract

import common._
import org.scalatest.{Matchers, WordSpec}
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.routing.Router
import uk.gov.hmrc.epayeapi.models.TaxYear
import uk.gov.hmrc.epayeapi.router.RoutesProvider

class GetPaymentHistoryWithAllocationsSpec
  extends WordSpec
    with Matchers
    with WSClientSetup
    with WiremockSetup
    with EmpRefGenerator
    with RestAssertions {

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder().overrides(bind[Router].toProvider[RoutesProvider]).build()

  "/organisations/epaye/{ton}/{tor}/payment-history/2016-17/allocations" should {
    val empRef = randomEmpRef()
    implicit val taxYear = TaxYear(2016)

    val paymentHistoryWithAllocationsSchemaPath = s"${app.path.toURI}/resources/public/api/conf/1.0/schemas/PaymentHistoryWithAllocations.schema.json"

    val paymentHistoryWithAllocationsUrl =
      s"$baseUrl/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/payment-history/${taxYear.asString}/allocations"

    "return a response body that conforms to the Payment History schema" in {
      given()
        .clientWith(empRef).isAuthorized
        .and()
        .epayePaymentHistoryWithAllocationsReturns(Fixtures.epayePaymentHistoryWithAllocations(empRef, taxYear))
        .when
        .get(paymentHistoryWithAllocationsUrl).withAuthHeader()
        .thenAssertThat()
        .bodyIsOfSchema(paymentHistoryWithAllocationsSchemaPath)
    }
  }
}
