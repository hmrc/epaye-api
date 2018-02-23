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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.EmpRef
import uk.gov.hmrc.epayeapi.models.TaxYear

object Fixtures {

  def epayeMasterData(empRef: EmpRef, taxYear: TaxYear): String =
    s"""
      |{
      |  "accountsOfficeReference": "${empRef.toString}",
      |  "yearRegistered": {
      |    "yearFrom": ${taxYear.yearFrom}
      |  }
      |}
    """.stripMargin

  val epayeAnnualStatement: String =
    """
      |{
      |  "rti": {
      |    "lineItems": [
      |      {
      |        "taxYear": {
      |          "yearFrom": 2017
      |        },
      |        "taxMonth": {
      |          "month": 7
      |        },
      |        "charges":1200,
      |        "payments": 0,
      |        "credits": 0,
      |        "writeOffs": 0,
      |        "balance": 1200,
      |        "dueDate": "2017-11-22",
      |        "isSpecified": false,
      |        "itemType": "month"
      |      },
      |      {
      |        "taxYear": {
      |          "yearFrom": 2017
      |        },
      |        "charges":750,
      |        "payments": 300,
      |        "credits": 200,
      |        "writeOffs": 50,
      |        "balance": 200,
      |        "dueDate": "2017-04-22",
      |        "isSpecified": false,
      |        "itemType": "eyu"
      |      },
      |      {
      |        "taxYear": {
      |          "yearFrom": 2017
      |        },
      |        "taxMonth": {
      |          "month": 3
      |        },
      |        "charges":750,
      |        "payments": 300,
      |        "credits": 200,
      |        "writeOffs": 50,
      |        "balance": 200,
      |        "dueDate": "2017-07-22",
      |        "isSpecified": true
      |      }
      |    ],
      |    "totals": {
      |      "charges":3200,
      |      "payments": 600,
      |      "credits": 400,
      |      "writeOffs": 100,
      |      "balance": 2100
      |    }
      |  },
      |  "nonRti": {
      |    "lineItems": [
      |      {
      |        "taxYear": {
      |          "yearFrom": 2017
      |        },
      |        "charges":300,
      |        "payments": 30,
      |        "credits": 70,
      |        "writeOffs": 0,
      |        "balance": 200,
      |        "dueDate": "2017-07-22",
      |        "isSpecified": false,
      |        "itemType": "1481",
      |        "codeText": "NON_RTI_CIS_FIXED_PENALTY"
      |      }
      |    ],
      |    "totals": {
      |      "charges":0,
      |      "payments": 0,
      |      "credits": 0,
      |      "writeOffs": 0,
      |      "balance": 0
      |    }
      |  },
      |  "unallocated": 2000
      |}
    """.stripMargin

  def epayePaymentHistory(empRef: EmpRef, taxYear: TaxYear): String =
    s"""
       |{
       |  "payments": [
       |    {
       |      "dateOfPayment": "${taxYear.yearFrom}-06-17",
       |      "amount": "123.45"
       |    },
       |    {
       |      "dateOfPayment": "${taxYear.yearFrom}-10-07",
       |      "amount": "456.78"
       |    },
       |    {
       |      "dateOfPayment": "${taxYear.yearFrom}-12-08",
       |      "amount": "999.00"
       |    }
       |  ]
       |}
    """.stripMargin

  def epayePaymentHistoryWithAllocations(empRef: EmpRef, taxYear: TaxYear): String =
    s"""
       |{
       |  "taxYear": {
       |    "yearFrom": ${taxYear.yearFrom}
       |  },
       |  "payments": [
       |    {
       |      "paymentDate": "${taxYear.yearFrom}-06-17",
       |      "method": "CHEQUE RECEIPTS",
       |      "amount": 123.45,
       |      "allocatedAmount": 123.45,
       |      "allocations": [
       |        {
       |          "period": {
       |            "taxFrom": "${taxYear.firstDay}",
       |            "taxTo": "${taxYear.firstDay.plusMonths(1).minusDays(1)}"
       |          },
       |          "amount": 123.45
       |        }
       |      ]
       |    },
       |    {
       |      "paymentDate": "${taxYear.yearFrom}-10-07",
       |      "method": "BACS RECEIPTS",
       |      "amount": 456.78,
       |      "allocatedAmount": 456.78,
       |      "allocations": [
       |        {
       |          "period": {
       |            "taxFrom": "${taxYear.firstDay.plusMonths(2)}",
       |            "taxTo": "${taxYear.firstDay.plusMonths(3).minusDays(1)}"
       |          },
       |          "amount": 456.78,
       |          "code": "NON_RTI_ERS_PENALTY_2"
       |        }
       |      ]
       |    },
       |    {
       |      "paymentDate": "${taxYear.yearFrom}-12-08",
       |      "method": "TPS RECEIPTS BY DEBIT CARD",
       |      "amount": 999.00,
       |      "allocatedAmount": "888.00",
       |      "allocations": [
       |        {
       |          "period": {
       |            "taxFrom": "${taxYear.firstDay.plusMonths(2)}",
       |            "taxTo": "${taxYear.firstDay.plusMonths(3).minusDays(1)}"
       |          },
       |          "amount": 444.00,
       |          "code": "NON_RTI_APPRENTICESHIP_LEVY_INTEREST"
       |        },
       |        {
       |          "period": {
       |            "taxFrom": "${taxYear.firstDay.plusMonths(2)}",
       |            "taxTo": "${taxYear.firstDay.plusMonths(3).minusDays(1)}"
       |          },
       |          "amount": 111.00,
       |          "code": "NON_RTI_EI_LATE_REPORT_DAILY_PENALTY"
       |        },
       |        {
       |          "period": {
       |            "taxFrom": "${taxYear.firstDay.plusMonths(1)}",
       |            "taxTo": "${taxYear.firstDay.plusMonths(2).minusDays(1)}"
       |          },
       |          "amount": 333.00
       |        }
       |      ]
       |    }
       |  ]
       |}
    """.stripMargin

  def expectedAnnualStatementJson(empRef: EmpRef): JsValue = Json.parse(
    s"""
      |{
      |  "taxOfficeNumber": "${empRef.taxOfficeNumber}",
      |  "taxOfficeReference": "${empRef.taxOfficeReference}",
      |  "taxYear": {
      |    "year": "2017-18",
      |    "firstDay": "2017-04-06",
      |    "lastDay": "2018-04-05"
      |  },
      |  "nonRtiCharges": [
      |    {
      |      "code": "NON_RTI_CIS_FIXED_PENALTY",
      |      "amount": 300,
      |      "clearedByCredits": 70,
      |      "clearedByPayments": 30,
      |      "clearedByWriteOffs": 0,
      |      "balance": 200,
      |      "dueDate": "2017-07-22"
      |    }
      |  ],
      |  "_embedded": {
      |    "earlierYearUpdate": {
      |      "amount": 750,
      |      "clearedByCredits": 200,
      |      "clearedByPayments": 300,
      |      "clearedByWriteOffs": 50,
      |      "balance": 200,
      |      "dueDate": "2017-04-22"
      |    },
      |    "rtiCharges": [
      |      {
      |        "taxMonth": {
      |          "month": 7,
      |          "firstDay": "2017-10-06",
      |          "lastDay": "2017-11-05"
      |        },
      |        "amount": 1200,
      |        "clearedByCredits": 0,
      |        "clearedByPayments": 0,
      |        "clearedByWriteOffs": 0,
      |        "balance": 1200,
      |        "dueDate": "2017-11-22",
      |        "isSpecified": false,
      |        "_links": {
      |          "self": {
      |            "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2017-18/7"
      |          }
      |        }
      |      },
      |      {
      |        "taxMonth": {
      |          "month": 3,
      |          "firstDay": "2017-06-06",
      |          "lastDay": "2017-07-05"
      |        },
      |        "amount": 750,
      |        "clearedByCredits": 200,
      |        "clearedByPayments": 300,
      |        "clearedByWriteOffs": 50,
      |        "balance": 200,
      |        "dueDate": "2017-07-22",
      |        "isSpecified": true,
      |        "_links": {
      |          "self": {
      |            "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2017-18/3"
      |          }
      |        }
      |      }
      |    ]
      |  },
      |  "_links": {
      |    "empRefs": {
      |      "href": "/organisations/paye/"
      |    },
      |    "summary": {
      |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}"
      |    },
      |    "statements": {
      |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements"
      |    },
      |    "self": {
      |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2017-18"
      |    },
      |    "next": {
      |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2018-19"
      |    },
      |    "previous": {
      |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2016-17"
      |    }
      |  }
      |}
     """.stripMargin
  )

  def expectedStatementLinksJson(empRef: EmpRef): JsValue = Json.parse(
    s"""
       |{
       |  "taxOfficeNumber": "${empRef.taxOfficeNumber}",
       |  "taxOfficeReference": "${empRef.taxOfficeReference}",
       |  "_embedded": {
       |    "statements": [{
       |      "taxYear": {
       |        "year": "2016-17",
       |        "firstDay": "2016-04-06",
       |        "lastDay": "2017-04-05"
       |      },
       |      "_links": {
       |        "self": {
       |          "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2016-17"
       |        }
       |      }
       |    }, {
       |      "taxYear": {
       |        "year": "2015-16",
       |        "firstDay": "2015-04-06",
       |        "lastDay": "2016-04-05"
       |      },
       |      "_links": {
       |        "self": {
       |          "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2015-16"
       |        }
       |      }
       |    }, {
       |      "taxYear": {
       |        "year": "2014-15",
       |        "firstDay": "2014-04-06",
       |        "lastDay": "2015-04-05"
       |      },
       |      "_links": {
       |        "self": {
       |          "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements/2014-15"
       |        }
       |      }
       |    }]
       |  },
       |  "_links": {
       |    "empRefs": {
       |      "href": "/organisations/paye/"
       |    },
       |    "self": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements"
       |    }
       |  }
       |}
     """.stripMargin
  )

  def expectedPaymentHistoryJson(empRef: EmpRef, taxYear: TaxYear): JsValue = Json.parse(
    s"""
       |{
       |  "taxOfficeNumber": "${empRef.taxOfficeNumber}",
       |  "taxOfficeReference": "${empRef.taxOfficeReference}",
       |  "taxYear": {
       |    "year": "${taxYear.asString}",
       |    "firstDay": "${taxYear.firstDay}",
       |    "lastDay": "${taxYear.lastDay}"
       |  },
       |  "payments": [
       |    {
       |      "paymentDate": "${taxYear.yearFrom}-12-08",
       |      "amount": 999.00
       |    },
       |    {
       |      "paymentDate": "${taxYear.yearFrom}-10-07",
       |      "amount": 456.78
       |    },
       |    {
       |      "paymentDate": "${taxYear.yearFrom}-06-17",
       |      "amount": 123.45
       |    }
       |  ],
       |  "_links": {
       |    "empRefs": {
       |      "href": "/organisations/paye/"
       |    },
       |    "summary": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}"
       |    },
       |    "statements": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements"
       |    },
       |    "self": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/payment-history/${taxYear.asString}"
       |    },
       |    "next": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/payment-history/${taxYear.next.asString}"
       |    },
       |    "previous": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/payment-history/${taxYear.previous.asString}"
       |    }
       |  }
       |}
     """.stripMargin
  )

  def expectedPaymentHistoryWithAllocationsJson(empRef: EmpRef, taxYear: TaxYear): JsValue = Json.parse(
    s"""
       |{
       |  "taxOfficeNumber": "${empRef.taxOfficeNumber}",
       |  "taxOfficeReference": "${empRef.taxOfficeReference}",
       |  "taxYear": {
       |    "year": "${taxYear.asString}",
       |    "firstDay": "${taxYear.firstDay}",
       |    "lastDay": "${taxYear.lastDay}"
       |  },
       |  "payments": [
       |    {
       |      "paymentDate": "${taxYear.yearFrom}-12-08",
       |      "method": "Debit Card",
       |      "amount": 999.00,
       |      "allocatedAmount": 888.00,
       |      "allocations": [
       |        {
       |          "taxYear": {
       |            "year": "${taxYear.asString}",
       |            "firstDay": "${taxYear.yearFrom}-04-06",
       |            "lastDay": "${taxYear.yearFrom+1}-04-05"
       |          },
       |          "taxMonth": {
       |            "month": 3,
       |            "firstDay": "${taxYear.yearFrom}-06-06",
       |            "lastDay": "${taxYear.yearFrom}-07-05"
       |          },
       |          "amount": 444.00,
       |          "code": "NON_RTI_APPRENTICESHIP_LEVY_INTEREST"
       |        },
       |        {
       |          "taxYear": {
       |            "year": "${taxYear.asString}",
       |            "firstDay": "${taxYear.yearFrom}-04-06",
       |            "lastDay": "${taxYear.yearFrom + 1}-04-05"
       |          },
       |          "taxMonth": {
       |            "month": 3,
       |            "firstDay": "${taxYear.yearFrom}-06-06",
       |            "lastDay": "${taxYear.yearFrom}-07-05"
       |          },
       |          "amount": 111.00,
       |          "code": "NON_RTI_EI_LATE_REPORT_DAILY_PENALTY"
       |        },
       |        {
       |          "taxYear": {
       |            "year": "${taxYear.asString}",
       |            "firstDay": "${taxYear.yearFrom}-04-06",
       |            "lastDay": "${taxYear.yearFrom+1}-04-05"
       |          },
       |          "taxMonth": {
       |            "month": 2,
       |            "firstDay": "${taxYear.yearFrom}-05-06",
       |            "lastDay": "${taxYear.yearFrom}-06-05"
       |          },
       |          "amount": 333.00
       |        }
       |      ]
       |    },
       |    {
       |      "paymentDate": "${taxYear.yearFrom}-10-07",
       |      "method": "BACS",
       |      "amount": 456.78,
       |      "allocatedAmount": 456.78,
       |      "allocations": [
       |        {
       |          "taxYear": {
       |            "year": "${taxYear.asString}",
       |            "firstDay": "${taxYear.yearFrom}-04-06",
       |            "lastDay": "${taxYear.yearFrom+1}-04-05"
       |          },
       |          "taxMonth": {
       |            "month": 3,
       |            "firstDay": "${taxYear.yearFrom}-06-06",
       |            "lastDay": "${taxYear.yearFrom}-07-05"
       |          },
       |          "amount": 456.78,
       |          "code": "NON_RTI_ERS_PENALTY_2"
       |        }
       |      ]
       |    },
       |    {
       |      "paymentDate": "${taxYear.yearFrom}-06-17",
       |      "method": "Cheque",
       |      "amount": 123.45,
       |      "allocatedAmount": 123.45,
       |      "allocations": [
       |        {
       |          "taxYear": {
       |            "year": "${taxYear.asString}",
       |            "firstDay": "${taxYear.yearFrom}-04-06",
       |            "lastDay": "${taxYear.yearFrom+1}-04-05"
       |          },
       |          "taxMonth": {
       |            "month": 1,
       |            "firstDay": "${taxYear.yearFrom}-04-06",
       |            "lastDay": "${taxYear.yearFrom}-05-05"
       |          },
       |          "amount": 123.45
       |        }
       |      ]
       |    }
       |  ],
       |  "_links": {
       |    "empRefs": {
       |      "href": "/organisations/paye/"
       |    },
       |    "summary": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}"
       |    },
       |    "statements": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/statements"
       |    },
       |    "payments": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/payment-history/${taxYear.asString}"
       |    },
       |    "self": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/payment-history/${taxYear.asString}/allocations"
       |    },
       |    "next": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/payment-history/${taxYear.next.asString}/allocations"
       |    },
       |    "previous": {
       |      "href": "/organisations/paye/${empRef.taxOfficeNumber}/${empRef.taxOfficeReference}/payment-history/${taxYear.previous.asString}/allocations"
       |    }
       |  }
       |}
     """.stripMargin
  )

  def authorisedEnrolmentJson(empRef: EmpRef): String = {
    s"""
       |{
       |  "authorisedEnrolments": [
       |    {
       |      "key": "IR-PAYE",
       |      "identifiers": [
       |        {
       |          "key": "TaxOfficeNumber",
       |          "value": "${empRef.taxOfficeNumber}"
       |        },
       |        {
       |          "key": "TaxOfficeReference",
       |          "value": "${empRef.taxOfficeReference}"
       |        }],
       |      "state": "Activated",
       |      "confidenceLevel": 0,
       |      "delegatedAuthRule": "epaye-auth",
       |      "enrolment": "IR-PAYE"
       |    }
       |  ]
       |}
      """.stripMargin
  }

  def authorised: String = {
    """
      |{
      |  "authorisedEnrolments": []
      |}
    """.stripMargin
  }

}
