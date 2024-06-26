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

package util

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.customs.declaration.model.upscan._
import util.TestData.FileReferenceOne

import java.net.URL
import java.time.Instant
import scala.xml.Elem

object UpscanNotifyTestData {
  def readyJson(fileReference: FileReference = FileReferenceOne): JsValue = Json.parse(
    s"""{
      |      "reference" : "${fileReference.toString}",
      |      "downloadUrl" : "http://remotehost/bucket/123",
      |      "fileStatus": "READY",
      |      "uploadDetails": {
      |        "uploadTimestamp": "2018-04-24T09:30:00Z",
      |        "checksum": "1a2b3c4d5e",
      |         "fileMimeType": "application/pdf",
      |         "fileName": "test.pdf"
      |      }
      |}
      |""".stripMargin)

  val FailedJson: JsValue = Json.parse(
    s"""{
      |       "reference" : "${FileReferenceOne.toString}",
      |       "fileStatus" : "FAILED",
      |       "failureDetails" : {
      |         "failureReason" : "QUARANTINE",
      |         "message" : "This file has a virus"
      |       }
      |}""".stripMargin)

  val FailedJsonWithInvalidFileStatus: JsValue = Json.parse(
    s"""{
      |       "reference" : "${FileReferenceOne.toString}",
      |       "fileStatus" : "INVALID_FILE_STATUS",
      |       "failureDetails" : {
      |         "failureReason" : "QUARANTINE",
      |         "message" : "This file has a virus"
      |       }
      |}""".stripMargin)

  val DownloadUrl: URL = new URL("http://remotehost/bucket/123")
  val UploadedTimestamp: Instant = Instant.parse("2018-04-24T09:30:00Z")
  val uploadDetails: UploadedDetails = UploadedDetails("test.pdf", "application/pdf", UploadedTimestamp, "1a2b3c4d5e")
  val ReadyCallbackBody: UploadedReadyCallbackBody = UploadedReadyCallbackBody(FileReferenceOne, DownloadUrl, ReadyFileStatus, uploadDetails)
  val ErrorDetails: UploadedErrorDetails = UploadedErrorDetails("QUARANTINE", "This file has a virus")
  val FailedCallbackBody: UploadedFailedCallbackBody = UploadedFailedCallbackBody(FileReferenceOne, FailedFileStatus, ErrorDetails)

  val UpscanNotificationInternalServerErrorJson = """{"code":"INTERNAL_SERVER_ERROR","message":"Internal server error"}"""
  val UpscanNotificationBadRequestJson = """{"code":"BAD_REQUEST","message":"Invalid upscan notification"}"""
  val UpscanNotificationBadRequestJsonPayload = """{"code":"BAD_REQUEST","message":"Invalid JSON payload"}"""
  val UpscanNotifyClientSubscriptionIdErrorJson = """{"code":"BAD_REQUEST","message":"Invalid clientSubscriptionId"}"""



  val UpscanNotificationFailedCustomsNotificationXml: Elem =
    <Root xmlns="hmrc:fileupload">
      <FileReference>{FileReferenceOne.toString}</FileReference>
      <Outcome>QUARANTINE</Outcome>
      <Details>This file has a virus</Details>
    </Root>

  val FileUploadInternalErrorNotificationXml: Elem =
    <errorResponse>
      <code>INTERNAL_SERVER_ERROR</code>
      <message>File upload for file reference {FileReferenceOne.toString} failed. A system error has prevented your document from being accepted. Please follow the guidance on www.gov.uk and submit your documents by an alternative method.</message>
    </errorResponse>

}
