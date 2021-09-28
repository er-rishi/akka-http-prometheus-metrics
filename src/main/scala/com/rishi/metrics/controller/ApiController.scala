package com.rishi.metrics.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, parameters, path}
import akka.http.scaladsl.server.Route
import com.rishi.metrics.custom.CustomHttpMetricsDirectives.{pathLabeled, userIdLabel}

class ApiController {
  lazy val routes: Route = userRoute

  def userRoute: Route = get {
    path("user") {
      parameters('userId) { userId =>
        userIdLabel(userId) {
          complete(StatusCodes.OK, "This is an application to add a custom metrics")
        }
      }
    }
  }
}

object ApiController {
  val routes: Route = new ApiController().routes
}