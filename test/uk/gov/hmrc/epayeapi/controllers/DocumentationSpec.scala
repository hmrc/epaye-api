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

import play.api.libs.json.{JsArray, JsString, JsSuccess}
import play.api.test.FakeRequest
import unit.AppSpec
import play.api.test.Helpers._

class DocumentationSpec extends AppSpec {
  "The API definition endpoint" should {
    "include whitelisted applications" in new App(builder.withConfig("whitelistedApplications" -> "my-application-id, my-other-application-id").build) {
      val result = contentAsJson(inject[DocumentationController].definition()(FakeRequest()))

      val lookupResult = (result \\ "whitelistedApplicationIds" ).head
      lookupResult shouldEqual JsArray(Seq(JsString("my-application-id"),
                                           JsString("my-other-application-id")))
    }
    "work without whitelisted applications" in new App(builder.build) {
      val result = contentAsJson(inject[DocumentationController].definition()(FakeRequest()))

      val lookupResult = (result \\ "whitelistedApplicationIds" ).head
      lookupResult shouldEqual JsArray()
    }
  }
}
