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

import javax.inject.{Inject, Singleton}

import play.api.http.HttpErrorHandler
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.api.controllers.{DocumentationController => ApiDocsController}
import uk.gov.hmrc.epayeapi.config.{AppContext, Startup}

@Singleton
case class DocumentationController @Inject()(
  httpErrorHandler: HttpErrorHandler,
  context: AppContext,
  startup: Startup
)
  extends ApiDocsController(httpErrorHandler) {

  // Leave here. This registers the service with service locator. We can't
  // run this in the body of 'Startup' due to a `import Play.current`
  // line in `WSRequest`
  startup.start()

  override def definition(): Action[AnyContent] = Action {
    Ok(views.txt.definition(context.apiContext, context.apiStatus, context.whitelistedApplications))
      .as("application/json")
  }

  def raml(version: String, file: String): Action[AnyContent] =
    conf(version, file)
}
