package com.karasiq.dropbox.oauth

import scala.collection.JavaConverters._
import scala.concurrent.{Future, Promise}
import scala.concurrent.duration._
import scala.language.postfixOps

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink}
import com.dropbox.core.{DbxAppInfo, DbxSessionStore, DbxWebAuth}

import com.karasiq.dropbox.model.Dropbox

private[oauth] class DefaultDropboxOAuth(port: Int, urlOpener: URLOpener)
                                        (implicit rc: Dropbox.RequestConfig, as: ActorSystem, mat: Materializer) extends DropboxOAuth {

  private[this] val http = Http()

  def authenticate(appKeys: Dropbox.AppKeys): Future[Dropbox.UserToken] = {
    val responsePromise = Promise[Dropbox.UserToken]

    val redirectUri = s"http://localhost:$port/"
    val sessionStore = new DbxSessionStore {
      var _value: String = _

      def set(value: String): Unit = {
        _value = value
      }

      def clear(): Unit = {
        _value = null
      }

      def get(): String = {
        _value
      }
    }

    val requestConfig = rc.toDbxRequestConfig
    val auth = new DbxWebAuth(requestConfig, new DbxAppInfo(appKeys.key, appKeys.secret))

    http.bind("localhost", port)
      .completionTimeout(5 minutes)
      .take(1)
      .alsoTo(Sink.onComplete(_.failed.foreach(responsePromise.tryFailure)))
      .runForeach { connection ⇒
        connection.handleWith {
          Flow[HttpRequest]
            .alsoTo(Sink.foreach { request ⇒
              val authFinish = auth.finishFromRedirect(redirectUri, sessionStore, request.uri.query().toMultiMap.mapValues(_.toArray).asJava)
              responsePromise.success(Dropbox.UserToken(authFinish.getAccessToken, authFinish.getUserId, authFinish.getUrlState))
            })
            .map(_ ⇒ HttpResponse(StatusCodes.OK, entity = "Success!"))
        }
      }

    val authRequest = DbxWebAuth.newRequestBuilder()
      .withRedirectUri(redirectUri, sessionStore)
      .build()

    val authUrl = auth.authorize(authRequest)
    urlOpener.openURL(authUrl)
    responsePromise.future
  }
}
