package com.rishi.metrics.custom

import akka.http.scaladsl.server.Directive
import akka.http.scaladsl.server.Directives._
import fr.davit.akka.http.metrics.core.scaladsl.server.HttpMetricsDirectives

trait CustomHttpMetricsDirectives extends HttpMetricsDirectives {

  def userIdLabel[L](userId: String): Directive[Unit] = {
    extractRequestContext.flatMap { _ =>
      mapResponseHeaders { headers =>
        val userIdHeader = UserIdLabelHeader(userId)
        headers :+ userIdHeader
      }
    }
  }
}

object CustomHttpMetricsDirectives extends CustomHttpMetricsDirectives
