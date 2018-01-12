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

package uk.gov.hmrc.epayeapi.models.in

import org.joda.time.LocalDate

case class EpayeMonthlyStatement(
  charges: EpayeMonthlyCharges,
  credits: EpayeMonthlyCredits,
  payments: EpayeMonthlyPaymentDetails,
  writeOffs: EpayeMonthlyChargesDetails,
  hasSpecifiedCharges: Boolean,
  balance: EpayeMonthlyBalance
)

case class EpayeMonthlyCharges(
  fps: EpayeMonthlyChargesDetails,
  cis: EpayeMonthlyChargesDetails,
  eps: EpayeMonthlyChargesDetails,
  others: BigDecimal,
  total: BigDecimal
)

case class EpayeMonthlyCredits(
  fps: EpayeMonthlyChargesDetails,
  cis: EpayeMonthlyChargesDetails,
  eps: EpayeMonthlyChargesDetails,
  total: BigDecimal
)

case class EpayeMonthlyChargesDetails(
  items: Seq[EpayeMonthlyStatementItem],
  total: BigDecimal
)

case class EpayeMonthlyStatementItem(
  code: EpayeCode,
  amount: BigDecimal
)

case class EpayeMonthlyPaymentDetails(
  items: Seq[EpayeMonthlyPaymentItem],
  total: BigDecimal
)

case class EpayeMonthlyPaymentItem(
  dateOfPayment: LocalDate,
  amount: BigDecimal
)

case class EpayeMonthlyBalance(
  total: BigDecimal,
  dueDate: LocalDate
)

