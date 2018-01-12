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

package uk.gov.hmrc.epayeapi.controllers

import play.api.Application
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.Formats._
import uk.gov.hmrc.epayeapi.models.out.{ApiErrorJson, EmpRefsJson}
import unit.AppSpec
import unit.auth.AuthComponents.{AuthFail, AuthOk}

import scala.concurrent.Future

class GetEmpRefsSpec extends AppSpec {
  val ton = EnrolmentIdentifier("TaxOfficeNumber", "840")
  val tor = EnrolmentIdentifier("TaxOfficeReference", "GZ00064")

  val activeEnrolment: Enrolment =
    Enrolment("IR-PAYE", Seq(ton, tor), "activated", Some("300"))

  val inactiveEnrolment: Enrolment =
    activeEnrolment.copy(state = "inactive")

  val differentEnrolment =
    Enrolment("IR-Else", Seq(ton, tor), "activated", Some("300"))


  def request(implicit a: Application): Future[Result] =
    inject[GetEmpRefsController].getEmpRefs()(FakeRequest())

  "The EmpRefs endpoint" should {
    "return 200 OK on active enrolments" in new App(build(AuthOk(activeEnrolment))) {
      status(request) shouldBe OK
    }
    "return a list of EmpRefs" in new App(build(AuthOk(activeEnrolment))) {
      contentAsJson(request) shouldBe Json.toJson(EmpRefsJson(apiBaseUrl, EmpRef(ton.value, tor.value)))
    }
    "return 200 OK on inactive enrolments" in new App(build(AuthOk(inactiveEnrolment))) {
      status(request) shouldBe OK
    }
    "return an empty list inactive EmpRefs" in new App(build(AuthOk(inactiveEnrolment))) {
      contentAsJson(request) shouldBe Json.toJson(EmpRefsJson.fromSeq(apiBaseUrl, Seq()))
    }
    "return 200 OK on different enrolments" in new App(build(AuthOk(differentEnrolment))) {
      status(request) shouldBe OK
    }
    "return an empty list on different enrolments" in new App(build(AuthOk(differentEnrolment))) {
      contentAsJson(request) shouldBe Json.toJson(EmpRefsJson.fromSeq(apiBaseUrl, Seq()))
    }
    "return 403 Forbidden on insufficient enrolments" in new App(build(AuthFail(new InsufficientEnrolments()))) {
      status(request) shouldBe FORBIDDEN
    }
    "explain the error on insufficient enrolments" in new App(build(AuthFail(new InsufficientEnrolments()))) {
      contentAsJson(request) shouldBe Json.toJson(ApiErrorJson.InsufficientEnrolments)
    }
  }

}


