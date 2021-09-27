package com.rishi.metrics.controller

import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server.Route
import fr.davit.akka.http.metrics.core.scaladsl.server.HttpMetricsDirectives.metrics
import fr.davit.akka.http.metrics.prometheus.marshalling.PrometheusMarshallers._
import fr.davit.akka.http.metrics.prometheus.{Buckets, PrometheusRegistry, PrometheusSettings, Quantiles}
import io.prometheus.client.CollectorRegistry


class MetricsController {

  import MetricsController._

  val route: Route = (get & path("admin" / "prometheus" / "metrics")) (metrics(registry))
}

object MetricsController {
  val routes: Route = new MetricsController().route

  val settings: PrometheusSettings = PrometheusSettings
    .default
    .withIncludePathDimension(true)
    .withIncludeMethodDimension(true)
    .withIncludeStatusDimension(true)
    .withDurationConfig(Buckets(1, 2, 3, 5, 8, 13, 21, 34))
    .withReceivedBytesConfig(Quantiles(0.5, 0.75, 0.9, 0.95, 0.99))
    .withSentBytesConfig(PrometheusSettings.DefaultQuantiles)
    .withDefineError(_.status.isFailure)

  val collector: CollectorRegistry = CollectorRegistry.defaultRegistry

  val registry: PrometheusRegistry = PrometheusRegistry(collector, settings)
}
