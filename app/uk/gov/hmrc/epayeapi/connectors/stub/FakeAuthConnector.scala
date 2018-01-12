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

package uk.gov.hmrc.epayeapi.connectors.stub

import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait FakeAuthConnector extends AuthConnector {
  def success: Any = ()
  def exception: Option[AuthorisationException] = None
  def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] = {
    exception.fold(Future.successful(success.asInstanceOf[A]))(Future.failed(_))
  }
}

object SandboxAuthConnector extends FakeAuthConnector {

  private def enrolment(empRef: EmpRef, activated: Boolean = true) = {
    new Enrolment("IR-PAYE", Seq(
      EnrolmentIdentifier("TaxOfficeNumber", empRef.taxOfficeNumber),
      EnrolmentIdentifier("TaxOfficeReference", empRef.taxOfficeReference)
    ),
      if (activated) "activated" else "inactive", Some("300"))
  }

  override val success = new Enrolments(
    Set(
      enrolment(EmpRef("001", "0000001")),
      enrolment(EmpRef("002", "0000002")),
      enrolment(EmpRef("003", "0000003")),
      enrolment(EmpRef("004", "0000004"), activated = false)
    )
  )
}

