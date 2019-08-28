/*
 * Copyright 2019 HM Revenue & Customs
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

package uk.gov.hmrc.customs.declaration.controllers

import controllers.Assets
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.http.MimeTypes
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.customs.api.common.controllers.DocumentationController
import uk.gov.hmrc.customs.declaration.logging.DeclarationsLogger

@Singleton
class DeclarationsDocumentationController @Inject()(assets: Assets,
                                                    cc: ControllerComponents,
                                                    configuration: Configuration,
                                                    logger: DeclarationsLogger)
  extends DocumentationController(assets, cc) {

  private lazy val mayBeV1WhitelistedApplicationIds = configuration.getOptional[Seq[String]]("api.access.version-1.0.whitelistedApplicationIds")
  private lazy val mayBeV2WhitelistedApplicationIds = configuration.getOptional[Seq[String]]("api.access.version-2.0.whitelistedApplicationIds")
  private lazy val mayBeV3WhitelistedApplicationIds = configuration.getOptional[Seq[String]]("api.access.version-3.0.whitelistedApplicationIds")

  private lazy val v2Enabled = configuration.getOptional[Boolean]("api.access.version-2.0.enabled").getOrElse(true)
  private lazy val v3Enabled = configuration.getOptional[Boolean]("api.access.version-3.0.enabled").getOrElse(true)

  def definition(): Action[AnyContent] = Action {
    logger.debugWithoutRequestContext(s"DeclarationsDocumentationController definition endpoint has been called")
    Ok(uk.gov.hmrc.customs.declaration.views.txt.definition(
      mayBeV1WhitelistedApplicationIds,
      mayBeV2WhitelistedApplicationIds,
      mayBeV3WhitelistedApplicationIds,
      v2Enabled,
      v3Enabled)).as(MimeTypes.JSON)
  }
}
