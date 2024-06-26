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

package uk.gov.hmrc.customs.declaration.model.filetransmission

import play.api.libs.json._
import uk.gov.hmrc.customs.declaration.model._
import uk.gov.hmrc.customs.declaration.model.upscan.{BatchId, FileReference, HttpUrlFormat}

import java.net.URL
import java.time.Instant

case class FileTransmissionBatch(
  id: BatchId,
  fileCount: Int
)
object FileTransmissionBatch {
  implicit val writes: OWrites[FileTransmissionBatch] = Json.writes[FileTransmissionBatch]
}

case class FileTransmissionFile(
  reference: FileReference,
  name: String,
  mimeType: String,
  checksum: String,
  location: URL,
  sequenceNumber: FileSequenceNo,
  size: Int = 1,
  uploadTimestamp: Instant
)
object FileTransmissionFile {
  implicit val urlFormat: HttpUrlFormat.type = HttpUrlFormat
  implicit val writes: OWrites[FileTransmissionFile] = Json.writes[FileTransmissionFile]
}

case class FileTransmissionInterface(
  name: String,
  version: String
)
object FileTransmissionInterface {
  implicit var writes: OWrites[FileTransmissionInterface] = Json.writes[FileTransmissionInterface]
}

case class FileTransmissionProperty(name: String, value: String)
object FileTransmissionProperty {
  implicit var writes: OWrites[FileTransmissionProperty] = Json.writes[FileTransmissionProperty]
}

case class FileTransmission(
  batch: FileTransmissionBatch,
  callbackUrl: URL,
  file: FileTransmissionFile,
  interface: FileTransmissionInterface,
  properties: Seq[FileTransmissionProperty]
)
object FileTransmission {
  implicit val urlFormat: HttpUrlFormat.type = HttpUrlFormat
  implicit val writes: OWrites[FileTransmission] = Json.writes[FileTransmission]
}

sealed trait FileTransmissionOutcome {
  val outcome: String
}
case object FileTransmissionSuccessOutcome extends FileTransmissionOutcome {
  override val outcome: String = "SUCCESS"
}
case object FileTransmissionFailureOutcome extends FileTransmissionOutcome {
  override val outcome: String = "FAILURE"
}

object FileTransmissionOutcome {
  implicit val readsFileTransmissionOutcome: Reads[FileTransmissionOutcome] = new Reads[FileTransmissionOutcome] {
    override def reads(json: JsValue): JsResult[FileTransmissionOutcome] = json match {
      case JsString(FileTransmissionSuccessOutcome.outcome) => JsSuccess(FileTransmissionSuccessOutcome)
      case JsString(FileTransmissionFailureOutcome.outcome) => JsSuccess(FileTransmissionFailureOutcome)
      case _ => JsError(s"Invalid FileTransmissionOutcome $json")
    }
  }
}

sealed trait FileTransmissionNotification {
  val fileReference: FileReference
  val batchId: BatchId
  val outcome: FileTransmissionOutcome
}

case class FileTransmissionSuccessNotification(fileReference: FileReference,
                                               batchId: BatchId,
                                               outcome: FileTransmissionOutcome = FileTransmissionSuccessOutcome
                                    ) extends FileTransmissionNotification

object FileTransmissionSuccessNotification {
  implicit val readsSuccessCallback: Reads[FileTransmissionSuccessNotification] = Json.reads[FileTransmissionSuccessNotification]
}

case class FileTransmissionFailureNotification(fileReference: FileReference,
                                               batchId: BatchId,
                                               outcome: FileTransmissionOutcome = FileTransmissionFailureOutcome,
                                               errorDetails: String
                                     ) extends FileTransmissionNotification

object FileTransmissionFailureNotification {
  implicit val readsFailureCallback: Reads[FileTransmissionFailureNotification] = Json.reads[FileTransmissionFailureNotification]
}

case class FileTransmissionCallbackDecider(outcome: FileTransmissionOutcome)

object FileTransmissionCallbackDecider {
  implicit val reads: Reads[FileTransmissionCallbackDecider] = Json.reads[FileTransmissionCallbackDecider]
  def parse(json: JsValue): JsResult[FileTransmissionNotification] = {
    json.validate[FileTransmissionCallbackDecider] match {
      case JsSuccess(decider, _) => decider.outcome match {
        case FileTransmissionSuccessOutcome => json.validate[FileTransmissionSuccessNotification]
        case FileTransmissionFailureOutcome => json.validate[FileTransmissionFailureNotification]
      }
      case error: JsError => error
    }
  }
}
