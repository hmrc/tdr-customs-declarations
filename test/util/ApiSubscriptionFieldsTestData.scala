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

package util

import java.util.UUID

import com.typesafe.config.{Config, ConfigFactory}
import uk.gov.hmrc.customs.declaration.model.{ApiSubscriptionFieldsResponse, ApiSubscriptionKey, FieldsId}
import util.CustomsDeclarationsExternalServicesConfig.ApiSubscriptionFieldsContext
import util.ExternalServicesConfig.{Host, Port}

trait ApiSubscriptionFieldsTestData {
  val fieldsIdString = "327d9145-4965-4d28-a2c5-39dedee50334"
  val fieldsId = FieldsId(fieldsIdString)
  val xClientId = "SOME_X_CLIENT_ID"
  val apiContext = "some/api/context"
  val apiContextEncoded = "some%2Fapi%2Fcontext"
  val apiVersion = "1.0"
  val apiSubscriptionKey = ApiSubscriptionKey(xClientId, apiContext, apiVersion)
  val apiSubscriptionKeyWithEncodedContext: ApiSubscriptionKey = apiSubscriptionKey.copy(context = apiContextEncoded)
  val apiSubscriptionFieldsResponse = ApiSubscriptionFieldsResponse(UUID.fromString(fieldsIdString))
  val responseJsonString: String =
    s"""{
       |  "clientId": "afsdknbw34ty4hebdv",
       |  "apiContext": "ciao-api",
       |  "apiVersion": "1.0",
       |  "fieldsId":"$fieldsIdString",
       |  "fields":{
       |    "callback-id":"http://localhost",
       |    "token":"abc123"
       |  }
       |}""".stripMargin

  lazy val validConfig: Config = ConfigFactory.parseString(
    s"""
       |Test {
       |  microservice {
       |    services {
       |      api-subscription-fields {
       |        host = $Host
       |        port = $Port
       |        context = $ApiSubscriptionFieldsContext
       |      }
       |    }
       |  }
       |}
    """.stripMargin)

  lazy val invalidConfigMissingHost: Config = ConfigFactory.parseString(
    s"""
       |Test {
       |  microservice {
       |    services {
       |      api-subscription-fields {
       |        port = $Port
       |        context = $ApiSubscriptionFieldsContext
       |      }
       |    }
       |  }
       |}
    """.stripMargin)

  lazy val invalidConfigMissingContext: Config = ConfigFactory.parseString(
    s"""
       |Test {
       |  microservice {
       |    services {
       |      api-subscription-fields {
       |        host = $Host
       |        port = $Port
       |      }
       |    }
       |  }
       |}
    """.stripMargin)

}

object ApiSubscriptionFieldsTestData extends ApiSubscriptionFieldsTestData
