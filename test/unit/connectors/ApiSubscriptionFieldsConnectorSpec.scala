/*
 * Copyright 2024 HM Revenue & Customs
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

package unit.connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, get, getRequestedFor, postRequestedFor, urlEqualTo}
import org.mockito.ArgumentMatchers.{any, anyString, eq => ameq}
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.HeaderNames
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsXml
import play.api.test.Helpers
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.customs.declaration.connectors.ApiSubscriptionFieldsConnector
import uk.gov.hmrc.customs.declaration.logging.DeclarationsLogger
import uk.gov.hmrc.customs.declaration.model._
import uk.gov.hmrc.customs.declaration.model.actionbuilders.ValidatedPayloadRequest
import uk.gov.hmrc.customs.declaration.services.DeclarationsConfigService
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.test.{HttpClientV2Support, WireMockSupport}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import uk.gov.hmrc.play.bootstrap.http.HttpClientV2Provider
import util.CustomsDeclarationsExternalServicesConfig.ApiSubscriptionFieldsContext
import util.ExternalServicesConfig._
import util.{ApiSubscriptionFieldsTestData, TestData, UnitSpec}

import scala.concurrent.{ExecutionContext, Future}

class ApiSubscriptionFieldsConnectorSpec extends UnitSpec
  with ScalaFutures
  with BeforeAndAfterEach
  with BeforeAndAfterAll
  with GuiceOneAppPerSuite
  with MockitoSugar
  with Eventually
  with WireMockSupport
  with HttpClientV2Support
  with ApiSubscriptionFieldsTestData {

  private val mockLogger = mock[DeclarationsLogger]
  private val mockDeclarationsConfigService = mock[DeclarationsConfigService]
  private val mockDeclarationsConfig = mock[DeclarationsConfig]
  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val vpr: ValidatedPayloadRequest[AnyContentAsXml] = TestData.TestCspValidatedPayloadRequest
  private implicit val ec: ExecutionContext = Helpers.stubControllerComponents().executionContext

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.api-subscription-fields.host" -> Host,
      "microservice.services.api-subscription-fields.port" -> Port,
    ).overrides(
      bind[HttpClientV2].toProvider[HttpClientV2Provider]
    ).build()

  lazy val connector: ApiSubscriptionFieldsConnector = app.injector.instanceOf[ApiSubscriptionFieldsConnector]

  private val expectedUrl = s"/field/application/SOME_X_CLIENT_ID/context/some/api/context/version/1.0"

  override protected def beforeEach(): Unit = {
    reset(mockLogger, mockDeclarationsConfigService)
    when(mockDeclarationsConfigService.declarationsConfig).thenReturn(mockDeclarationsConfig)
    when(mockDeclarationsConfig.apiSubscriptionFieldsBaseUrl).thenReturn(s"http://$Host:$Port$ApiSubscriptionFieldsContext")
  }

  "ApiSubscriptionFieldsConnector" can {
    "when making a successful request" should {
      "use the correct URL for valid path parameters and config" in {

        setupSuccessfulDeclarationRequest()

        val result = connector.getSubscriptionFields(apiSubscriptionKey).futureValue

        result shouldBe apiSubscriptionFieldsResponse

        wireMockServer.verify(
          1,
          getRequestedFor(urlEqualTo(expectedUrl))
        )
      }
    }

    "when making an failing request" should {
      "propagate an underlying error when api subscription fields call fails with a non-http exception" in {
        returnResponseForRequest(Future.failed(TestData.emulatedServiceFailure))

        val caught = intercept[TestData.EmulatedServiceFailure] {
          awaitRequest
        }

        caught shouldBe TestData.emulatedServiceFailure
      }
    }
  }

//  private def awaitRequest = {
//    await(connector.getSubscriptionFields(apiSubscriptionKey))
//  }
//
//  private def returnResponseForRequest(eventualResponse: Future[ApiSubscriptionFieldsResponse]) = {
//    when(mockWSGetImpl.GET[ApiSubscriptionFieldsResponse](anyString(), any(), any())
//      (any[HttpReads[ApiSubscriptionFieldsResponse]](), any[HeaderCarrier](), any[ExecutionContext])).thenReturn(eventualResponse)
//  }

  private def setupSuccessfulDeclarationRequest(): Unit = {
    wireMockServer.stubFor(get(urlEqualTo(expectedUrl))
      .willReturn(
        aResponse()
          .withStatus(OK)
          .withBody(responseJsonString)))
  }
}
