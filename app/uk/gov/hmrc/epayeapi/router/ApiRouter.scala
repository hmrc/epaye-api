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

package uk.gov.hmrc.epayeapi.router
import ApiRouter._
import javax.inject.{Inject, Singleton}

import play.api.routing.Router.Routes
import play.api.routing.sird._
import play.api.routing.{Router, SimpleRouter}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.controllers.{GetAnnualStatementController, GetEmpRefsController, GetMonthlyStatementController, GetSummaryController}
import uk.gov.hmrc.epayeapi.models.{ExtractTaxYear, TaxMonth}

@Singleton
case class ApiRouter @Inject() (
  prodRoutes: prod.Routes,
  getEmpRefsController: GetEmpRefsController,
  getTotalsController: GetSummaryController,
  getAnnualStatementController: GetAnnualStatementController,
  getMonthlyStatementController: GetMonthlyStatementController
) extends SimpleRouter {

  val appRoutes = Router.from {
    case GET(p"/") =>
      getEmpRefsController.getEmpRefs()

    case GET(p"/${TaxOfficeNumber(ton)}/${TaxOfficeReference(tor)}") =>
      getTotalsController.getSummary(EmpRef(ton, tor))

    case GET(p"/${TaxOfficeNumber(ton)}/${TaxOfficeReference(tor)}/statements/${ ExtractTaxYear(taxYear) }") =>
      getAnnualStatementController.getAnnualStatement(EmpRef(ton, tor), taxYear)

    case GET(p"/${TaxOfficeNumber(ton)}/${TaxOfficeReference(tor)}/statements/${ ExtractTaxYear(taxYear) }/${ int(month) }") if 1 <= month && month <= 12 =>
      getMonthlyStatementController.getStatement(EmpRef(ton, tor), taxYear, TaxMonth(taxYear, month))
  }

  val routes: Routes = prodRoutes.routes.orElse(appRoutes.routes)

}

object ApiRouter {
  object TaxOfficeNumber {
    val regex = "(\\d{3})".r
    def unapply(string: String): Option[String] = {
      string match {
        case regex(result) => Some(result)
        case _ => None
      }
    }
  }

  object TaxOfficeReference {
    val regex = "([0-9A-Z]{1,10})".r
    def unapply(string: String): Option[String] = {
      string match {
        case regex(result) => Some(result)
        case _ => None
      }
    }
  }
}