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

import javax.inject.{Inject, Singleton}

import com.google.inject.Provider
import play.api.http.HttpConfiguration
import play.api.routing.Router

@Singleton
class RoutesProvider @Inject()(
  apiRouter: ApiRouter,
  httpConfig: HttpConfiguration) extends Provider[Router] {

  lazy val get = apiRouter.withPrefix(httpConfig.context)
}
