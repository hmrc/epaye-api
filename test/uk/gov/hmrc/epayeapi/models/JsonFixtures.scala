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

package uk.gov.hmrc.epayeapi.models

import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.in._

import scala.io.Source

object JsonFixtures {
  def getResourceAsString(name: String): String =
    Source.fromURL(getClass.getResource(name), "utf-8").mkString("")

  object annualStatements {
    lazy val annualStatement: String = getResourceAsString("/epaye/annual-statement/annual-statement.json")
  }

  val emptyEpayeAnnualStatement =
    EpayeAnnualStatement(
      rti = AnnualStatementTable(
        lineItems = Seq(),
        totals = AnnualTotal(
          charges = 0,
          payments = 0,
          credits = 0,
          writeOffs = 0,
          balance = 0
        )
      ),
      nonRti = AnnualStatementTable(
        lineItems = Seq(),
        totals = AnnualTotal(
          charges = 0,
          payments = 0,
          credits = 0,
          writeOffs = 0,
          balance = 0
        )
      ),
      unallocated = None
    )
}
