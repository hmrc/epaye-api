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

package unit.auth

import uk.gov.hmrc.auth.core.{AuthorisationException, Enrolment, Enrolments}
import uk.gov.hmrc.epayeapi.connectors.stub.FakeAuthConnector

object AuthComponents {
  case class AuthOk(data: Enrolment) extends FakeAuthConnector {
    override val success = Enrolments(Set(data))
  }
  case class AuthFail(pureException: AuthorisationException) extends FakeAuthConnector {
    override val exception: Option[AuthorisationException] = Some(pureException)
  }
  case class AnyAuth[A](data: A) extends FakeAuthConnector {
    override val success = data
  }
}
