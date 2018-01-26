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

package uk.gov.hmrc.epayeapi.models.out

case class ApiErrorJson(code: String, message: String)

object ApiErrorJson {
  object InvalidBearerToken extends ApiErrorJson(
    "INVALID_BEARER_TOKEN",
    "You must provide a valid Bearer token in your header"
  )

  object MissingBearerToken extends ApiErrorJson(
    "MISSING_BEARER_TOKEN",
    "You must provide a valid Bearer token in your header"
  )

  object ExpiredBearerToken extends ApiErrorJson(
    "EXPIRED_BEARER_TOKEN",
    "You must provide a valid Bearer token in your header"
  )

  object AuthorisationError extends ApiErrorJson(
    "AUTHORISATION_ERROR",
    "You must provide a valid Bearer token in your header"
  )

  object InsufficientEnrolments extends ApiErrorJson(
    "INSUFFICIENT_ENROLMENTS",
    "You are not currently enrolled for ePAYE"
  )

  object InvalidEmpRef extends ApiErrorJson(
    "INVALID_EMPREF",
    "Provided EmpRef is not associated with your account"
  )

  object EmpRefNotFound extends ApiErrorJson(
    "EMPREF_NOT_FOUND",
    "Provided EmpRef wasn't found."
  )

  object InternalServerError extends ApiErrorJson(
    "INTERNAL_SERVER_ERROR",
    "We are currently experiencing problems. Please try again later."
  )
}
