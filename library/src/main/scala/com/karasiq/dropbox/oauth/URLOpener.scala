package com.karasiq.dropbox.oauth

trait URLOpener {
  def openURL(url: String): Unit
}

object URLOpener {
  object Desktop extends URLOpener {
    import java.net.URI

    val supported = java.awt.Desktop.isDesktopSupported

    def openURL(url: String): Unit = {
      /* if (supported) */ java.awt.Desktop.getDesktop.browse(new URI(url))
    }
  }

  object Console extends URLOpener {
    def openURL(url: String): Unit = {
      println("Please open URL in browser: " + url)
    }
  }

  object Default extends URLOpener {
    def openURL(url: String): Unit = {
      if (Desktop.supported) Desktop.openURL(url) else Console.openURL(url)
    }
  }
}
