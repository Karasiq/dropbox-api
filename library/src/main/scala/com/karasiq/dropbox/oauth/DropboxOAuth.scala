package com.karasiq.dropbox.oauth

import scala.concurrent.Future
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.stream.Materializer

import com.karasiq.dropbox.model.Dropbox

trait DropboxOAuth {
  def authenticate(appKeys: Dropbox.AppKeys): Future[Dropbox.UserToken]
}

object DropboxOAuth {
  val DefaultPort = 45742

  def apply(port: Int = DefaultPort, urlOpener: URLOpener = URLOpener.Default)
           (implicit rc: Dropbox.RequestConfig, as: ActorSystem, mat: Materializer): DropboxOAuth = {
    new DefaultDropboxOAuth(port, urlOpener)
  }
}