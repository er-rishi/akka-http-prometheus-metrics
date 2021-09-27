package com.rishi.metrics

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.knoldus.metrics.controller.MetricsController
import com.rishi.metrics.controller.{ApiController, MetricsController}
import fr.davit.akka.http.metrics.core.scaladsl.server.HttpMetricsRoute
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Application extends App {

  protected val logger: Logger = LoggerFactory.getLogger(this.getClass)

  implicit val system: ActorSystem = ActorSystem("AkkaHttpMetricsService")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val routes = ApiController.routes ~ MetricsController.routes

  val routesWithMetrics = HttpMetricsRoute(routes).recordMetrics(MetricsController.registry)
  val bindingFuture = Http().bindAndHandle(routesWithMetrics, "0.0.0.0", 8080)

  bindingFuture.onComplete {
    case Success(binding) ⇒
      val localAddress = binding.localAddress
      logger.info(s"Akka Http Metrics Service is listening on ${localAddress.getHostName}:${localAddress.getPort}!!!!")
    case Failure(exception) => logger.error("Unable to start Akka Http Metrics Service due to ", exception)
  }
}
