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

import uk.gov.hmrc.epayeapi.models.Formats._
import akka.stream.Materializer
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, Retrievals}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.out.ApiErrorJson.{AuthorizationHeaderInvalid, InsufficientEnrolments, InvalidEmpRef}
import uk.gov.hmrc.play.binders.SimpleObjectBinder
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.{ExecutionContext, Future}

trait ApiController extends BaseController with AuthorisedFunctions {
  val epayeEnrolment = Enrolment("IR-PAYE")
  val epayeRetrieval = Retrievals.authorisedEnrolments
  def authConnector: AuthConnector
  implicit def ec: ExecutionContext
  implicit def mat: Materializer

  def EnrolmentsAction(enrolment: Enrolment, retrieveEnrolments: Retrieval[Enrolments])(action: Enrolments => EssentialAction): EssentialAction = {
    EssentialAction { implicit request =>
      Accumulator.done {
        authorised(enrolment.withDelegatedAuthRule("epaye-auth"))
          .retrieve(retrieveEnrolments) { enrolments =>
            action(enrolments)(request).run()
          } recoverWith {
            case ex: MissingBearerToken => missingBearerToken
            case ex: InsufficientEnrolments => insufficientEnrolments
          }
      }
    }
  }

  def EmpRefsAction(action: Set[EmpRef] => EssentialAction): EssentialAction =
    EnrolmentsAction(epayeEnrolment, epayeRetrieval) { enrolments =>
      EssentialAction { request =>
        action(enrolments.enrolments.flatMap(enrolmentToEmpRef))(request)
      }
    }

  def EmpRefAction(empRefFromUrl: EmpRef)(action: EssentialAction): EssentialAction = {
    EmpRefsAction { empRefsFromAuth =>
      EssentialAction { request =>
        empRefsFromAuth.find(_ == empRefFromUrl) match {
          case Some(empRef) => action(request)
          case None => Accumulator.done(invalidEmpRef)
        }
      }
    }
  }

  def missingBearerToken: Future[Result] =
    Future.successful(Unauthorized(Json.toJson(AuthorizationHeaderInvalid)))
  def insufficientEnrolments: Future[Result] =
    Future.successful(Forbidden(Json.toJson(InsufficientEnrolments)))
  def invalidEmpRef: Future[Result] =
    Future.successful(Forbidden(Json.toJson(InvalidEmpRef)))

  private def enrolmentToEmpRef(enrolment: Enrolment): Option[EmpRef] = {
    for {
      "IR-PAYE" <- Option(enrolment.key)
      tn <- enrolment.identifiers.find(_.key == "TaxOfficeNumber")
      tr <- enrolment.identifiers.find(_.key == "TaxOfficeReference")
      if enrolment.isActivated
    } yield EmpRef(tn.value, tr.value)
  }
}

object ApiController {
  implicit val empRefPathBinder = new SimpleObjectBinder[EmpRef](EmpRef.fromIdentifiers, _.encodedValue)
}
