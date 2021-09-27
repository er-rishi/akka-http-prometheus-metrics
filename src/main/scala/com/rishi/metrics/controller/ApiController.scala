package com.rishi.metrics.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Route
import fr.davit.akka.http.metrics.core.scaladsl.server.HttpMetricsDirectives.pathLabeled

class ApiController {
  lazy val routes: Route = messageRoute

  def messageRoute: Route = get {
    pathLabeled("message", "message") {
      complete(StatusCodes.OK, "This is an application to show the way to expose the Prometheus metrics")
    }
  }
}

object ApiController {
  val routes: Route = new ApiController().routes
}