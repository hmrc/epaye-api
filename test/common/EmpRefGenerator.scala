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

package common

import uk.gov.hmrc.domain.EmpRef

import scala.util.Random

trait EmpRefGenerator {
  def getEmpRef: EmpRef = randomEmpRef()

  def randomEmpRef(excluding: EmpRef*): EmpRef =
    randomExcluding[EmpRef](excluding) {
      EmpRef(nextTaxOfficeNumber(), nextTaxOfficeReference())
    }

  def nextUppers(n: Int): String = {
    def nextUppers(sb: StringBuilder, n: Int): String =
      if (n == 0) sb.toString()
      else nextUppers(sb.append(nextUpperChar()), n - 1)

    nextUppers(new StringBuilder, n)
  }

  private def nextUpperChar(): Char =
    nextIntInRange('A', 'Z').toChar

  def nextDigits(n: Int): String = {
    def nextDigits(sb: StringBuilder, n: Int): String =
      if (n == 0)
        sb.toString()
      else
        nextDigits(sb.append(nextDigitChar()), n - 1)

    nextDigits(new StringBuilder, n)
  }

  private def nextDigitChar(): Char =
    nextIntInRange('0', '9').toChar
  private def nextTaxOfficeNumber(): String =
    nextDigits(3)

  private def nextTaxOfficeReference(): String =
    nextUppers(2) + nextDigits(5)

  private def randomExcluding[T](excluding: Seq[_ >: T])(generator: => T): T = {
    val generated = generator
    if (excluding.contains(generated))
      randomExcluding[T](excluding)(generator)
    else
      generated
  }

  private def nextIntInRange(from: Int, to: Int): Int =
    from + Random.nextInt(to - from + 1)
}

object EmpRefGenerator extends EmpRefGenerator
