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

import akka.stream.Materializer
import play.api.Logger
import play.api.libs.json.Json
import play.api.libs.streams.Accumulator
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.Formats._
import uk.gov.hmrc.epayeapi.models.out.ApiErrorJson
import uk.gov.hmrc.epayeapi.models.out.ApiErrorJson.{InsufficientEnrolments, InvalidEmpRef}
import uk.gov.hmrc.play.binders.SimpleObjectBinder
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext

trait ApiController extends BaseController with AuthorisedFunctions {
  val epayeEnrolment = Enrolment("IR-PAYE")
  val epayeRetrieval = Retrievals.authorisedEnrolments
  def authConnector: AuthConnector
  implicit def ec: ExecutionContext
  implicit def mat: Materializer

  def AuthorisedAction(enrolment: Enrolment)(action: => EssentialAction): EssentialAction =
    EssentialAction { implicit request =>
      Accumulator.done {
        authorised(enrolment) {
          action(request).run()
        } recover logAuthError(recoverAuthFailure)
      }
    }

  def EmpRefAction(empRefFromUrl: EmpRef)(action: EssentialAction): EssentialAction = {
    val enrolment = epayeEnrolment
      .withEmpRef(empRefFromUrl)
      .withDelegatedAuth

    AuthorisedAction(enrolment)(action)
  }

  def logAuthError(pf: PartialFunction[Throwable, Result]): PartialFunction[Throwable, Result] = {
    case ex: Throwable =>
      Logger.info("Recovering from auth error:", ex)
      pf(ex)
  }

  def recoverAuthFailure: PartialFunction[Throwable, Result] = {
    case ex: MissingBearerToken => missingBearerToken
    case ex: InvalidBearerToken => invalidBearerToken
    case ex: BearerTokenExpired => expiredBearerToken
    case ex: InsufficientEnrolments => insufficientEnrolments
    case ex: AuthorisationException => authorisationError
  }

  def authorisationError: Result =
    Unauthorized(Json.toJson(ApiErrorJson.AuthorisationError))
  def invalidBearerToken: Result =
    Unauthorized(Json.toJson(ApiErrorJson.InvalidBearerToken))
  def expiredBearerToken: Result =
    Unauthorized(Json.toJson(ApiErrorJson.ExpiredBearerToken))
  def missingBearerToken: Result =
    Unauthorized(Json.toJson(ApiErrorJson.MissingBearerToken))
  def insufficientEnrolments: Result =
    Forbidden(Json.toJson(InsufficientEnrolments))
  def invalidEmpRef: Result =
    Forbidden(Json.toJson(InvalidEmpRef))

  implicit class EnrolmentOps(val enrolment: Enrolment) {
    def withDelegatedAuth: Enrolment =
      enrolment.withDelegatedAuthRule("epaye-auth")

    def withEmpRef(empRef: EmpRef): Enrolment =
      enrolment
        .withIdentifier("TaxOfficeNumber", empRef.taxOfficeNumber)
        .withIdentifier("TaxOfficeReference", empRef.taxOfficeReference)
  }
}

object ApiController {
  implicit val empRefPathBinder = new SimpleObjectBinder[EmpRef](EmpRef.fromIdentifiers, _.encodedValue)
}
