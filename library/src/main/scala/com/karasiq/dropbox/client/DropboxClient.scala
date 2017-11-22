package com.karasiq.dropbox.client

import scala.concurrent.Future

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.dropbox.core.v2.files.{FileMetadata, Metadata}
import com.dropbox.core.v2.users.SpaceUsage

import com.karasiq.dropbox.model.Dropbox

trait DropboxClient {
  type PathT = String
  def spaceUsage(): Future[SpaceUsage]
  def list(path: PathT, recursive: Boolean = false): Source[Metadata, NotUsed]
  def upload(path: PathT): Sink[ByteString, Future[FileMetadata]]
  def download(path: PathT): Source[ByteString, NotUsed]
  def delete(path: PathT): Future[Metadata]
}

object DropboxClient {
  val DefaultDispatcherId = "dropbox.dispatcher"

  def apply(dispatcherId: String = DefaultDispatcherId)(implicit rc: Dropbox.RequestConfig, token: Dropbox.UserToken,
                                                        actorSystem: ActorSystem, mat: Materializer): DropboxClient = {
    implicit val dispatcher = actorSystem.dispatchers.lookup(dispatcherId)
    new DefaultDropboxClient()
  }
}
