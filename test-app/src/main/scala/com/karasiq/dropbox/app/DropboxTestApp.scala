package com.karasiq.dropbox.app

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import akka.util.ByteString

import com.karasiq.dropbox.client.DropboxClient
import com.karasiq.dropbox.model.Dropbox
import com.karasiq.dropbox.oauth.DropboxOAuth

object DropboxTestApp extends App {
  implicit val actorSystem = ActorSystem()
  implicit val materializer = ActorMaterializer()
  import materializer.executionContext

  implicit val requestConfig = Dropbox.RequestConfig()

  val oauth = DropboxOAuth()
  implicit val token = Await.result(oauth.authenticate(Dropbox.AppKeys()), Duration.Inf)

  val client = DropboxClient()
  client.spaceUsage().foreach(println)

  Source.single(ByteString("12234234324"))
    .runWith(client.upload("/test.txt"))
    .foreach(println)

  client.download("/test.txt").fold(ByteString.empty)(_ ++ _).map(_.utf8String).runForeach(println)
}

