package com.karasiq.dropbox.client

import scala.collection.JavaConverters._
import scala.concurrent.Future

import akka.actor.ActorSystem
import akka.dispatch.MessageDispatcher
import akka.stream.{ActorAttributes, Materializer}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, StreamConverters}
import akka.util.ByteString
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.Metadata

import com.karasiq.dropbox.model.Dropbox

private[client] class DefaultDropboxClient(implicit rc: Dropbox.RequestConfig, token: Dropbox.UserToken,
                                           actorSystem: ActorSystem, materializer: Materializer, dispatcher: MessageDispatcher) extends DropboxClient {

  private[this] val streamAttributes = ActorAttributes.dispatcher(dispatcher.id)
  private[this] val dropbox = new DbxClientV2(rc.toDbxRequestConfig, token.accessToken)

  def spaceUsage() = {
    Future(dropbox.users().getSpaceUsage)
  }

  def getMetadata(path: PathT) = {
    Future(dropbox.files().getMetadata(path))
  }

  def createDirectory(path: PathT) = {
    Future(dropbox.files().createFolderV2(path).getMetadata)
  }

  def list(path: PathT, recursive: Boolean) = {
    val stream = Source.fromIterator { () ⇒
      def nextIterator(cursor: String): Iterator[Metadata] = {
        val result = dropbox.files().listFolderContinue(cursor)
        val metadatas = result.getEntries.asScala.toIterator
        if (result.getHasMore) metadatas ++ nextIterator(result.getCursor) else metadatas
      }

      val result = dropbox.files().listFolderBuilder(path).withRecursive(recursive).start()
      val entries = result.getEntries.asScala.toIterator
      if (result.getHasMore) entries ++ nextIterator(result.getCursor) else entries
    }
    stream.withAttributes(streamAttributes).named("dropboxList")
  }

  def upload(path: PathT) = {
    Flow[ByteString]
      .prefixAndTail(0)
      .async
      .map { case (_, byteStream) ⇒
        val inputStream = byteStream
          .toMat(StreamConverters.asInputStream())(Keep.right)
          .run()
        dropbox.files().upload(path).uploadAndFinish(inputStream)
      }
      .toMat(Sink.head)(Keep.right)
      .withAttributes(streamAttributes)
      .named("dropboxUpload")
  }

  def download(path: PathT) = {
    Source.single(path)
      .flatMapConcat { path ⇒ StreamConverters.fromInputStream(() ⇒ dropbox.files().download(path).getInputStream) }
      .withAttributes(streamAttributes)
      .named("dropboxDownload")
  }

  def delete(path: PathT) = Future {
    dropbox.files().deleteV2(path).getMetadata
  }
}
