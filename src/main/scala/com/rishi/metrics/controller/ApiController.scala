package com.rishi.metrics.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{complete, get, parameters, path}
import akka.http.scaladsl.server.Route
import com.rishi.metrics.custom.{TotalRequestByUserIdMetric, UserIdDimension}
import fr.davit.akka.http.metrics.core.scaladsl.server.HttpMetricsDirectives.pathLabeled

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApiController {
  lazy val routes: Route = userRoute

  def userRoute: Route = get {
    pathLabeled("user", "getUser") {
      parameters('userId) { userId =>
        Future(TotalRequestByUserIdMetric.inc(List(UserIdDimension(userId))))
        complete(StatusCodes.OK, "This is an application to add a custom metrics")
      }
    }
  }
}

object ApiController {
  val routes: Route = new ApiController().routes
}