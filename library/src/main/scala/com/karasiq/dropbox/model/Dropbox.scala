package com.karasiq.dropbox.model

import akka.actor.ActorSystem
import com.dropbox.core.{DbxAppInfo, DbxAuthFinish, DbxRequestConfig}

import com.karasiq.common.configs.ConfigImplicits._

object Dropbox {
  final case class RequestConfig(appName: String, maxRetries: Int) {
    def toDbxRequestConfig = {
      DbxRequestConfig.newBuilder(appName)
        .withAutoRetryEnabled(maxRetries)
        .build()
    }
  }

  object RequestConfig {
    def apply(config: Config): RequestConfig = {
      RequestConfig(config.getString("app-name"), config.getInt("max-retries"))
    }

    def apply()(implicit as: ActorSystem): RequestConfig = {
      apply(as.settings.config.getConfig("dropbox.requests"))
    }
  }

  final case class AppKeys(key: String, secret: String) {
    def toDbxAppInfo = {
      new DbxAppInfo(key, secret)
    }
  }

  object AppKeys {
    def apply(config: Config): AppKeys = {
      AppKeys(config.getString("key"), config.getString("secret"))
    }

    def apply()(implicit as: ActorSystem): AppKeys = {
      apply(as.settings.config.getConfig("dropbox.app-keys"))
    }
  }

  final case class UserToken(accessToken: String, userId: String, urlState: String) {
    def toDbxAuthFinish = {
      new DbxAuthFinish(accessToken, userId, urlState)
    }
  }
}
