package com.rishi.metrics.custom

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import fr.davit.akka.http.metrics.core.HttpMetricsRegistry.{MethodDimension, PathDimension, StatusGroupDimension}
import fr.davit.akka.http.metrics.core._
import fr.davit.akka.http.metrics.prometheus.Quantiles.Quantile
import fr.davit.akka.http.metrics.prometheus.{Buckets, PrometheusConverters, PrometheusSettings, Quantiles}
import io.prometheus.client.CollectorRegistry

import scala.concurrent.duration.Deadline
import scala.concurrent.{ExecutionContext, Future}

object PrometheusRegistryCustom {

  implicit private class RichSummaryBuilder(val builder: io.prometheus.client.Summary.Builder) extends AnyVal {

    def quantiles(qs: Quantile*): io.prometheus.client.Summary.Builder = {
      qs.foldLeft(builder) {
        case (b, q) => b.quantile(q.percentile, q.error)
      }
    }

  }

  def apply(
             underlying: CollectorRegistry = CollectorRegistry.defaultRegistry,
             settings: PrometheusSettings = PrometheusSettings.default
           ): PrometheusRegistryCustom = {
    new PrometheusRegistryCustom(settings, underlying)
  }
}


class PrometheusRegistryCustom(settings: PrometheusSettings, val underlying: CollectorRegistry) extends HttpMetricsRegistry(settings) {

  import PrometheusConverters._
  import PrometheusRegistryCustom._

  private val labels: Seq[String] = {
    val methodLabel = if (settings.includeMethodDimension) Some(MethodDimension.Key) else None
    val pathLabel = if (settings.includePathDimension) Some(PathDimension.Key) else None
    val statusLabel = if (settings.includeStatusDimension) Some(StatusGroupDimension.Key) else None
    val userIdLabel = Some("userId")
    (methodLabel ++ pathLabel ++ statusLabel ++ userIdLabel).toSeq
  }

  override lazy val active: Gauge = io.prometheus.client.Gauge
    .build()
    .namespace(settings.namespace)
    .name("requests_active")
    .help("Active HTTP requests")
    .register(underlying)

  override lazy val requests: Counter = io.prometheus.client.Counter
    .build()
    .namespace(settings.namespace)
    .name("requests_total")
    .help("Total HTTP requests")
    .register(underlying)

  override lazy val receivedBytes: Histogram = {
    val name = "requests_size_bytes"
    val help = "HTTP request size"
    settings.receivedBytesConfig match {
      case Quantiles(qs, maxAge, ageBuckets) =>
        io.prometheus.client.Summary
          .build()
          .namespace(settings.namespace)
          .name(name)
          .help(help)
          .quantiles(qs: _*)
          .maxAgeSeconds(maxAge.toSeconds)
          .ageBuckets(ageBuckets)
          .register(underlying)

      case Buckets(bs) =>
        io.prometheus.client.Histogram
          .build()
          .namespace(settings.namespace)
          .name(name)
          .help(help)
          .buckets(bs: _*)
          .register(underlying)
    }
  }

  override lazy val responses: Counter = io.prometheus.client.Counter
    .build()
    .namespace(settings.namespace)
    .name("responses_total")
    .help("HTTP responses")
    .labelNames(labels: _*)
    .register(underlying)

  override lazy val errors: Counter = io.prometheus.client.Counter
    .build()
    .namespace(settings.namespace)
    .name("responses_errors_total")
    .help("Total HTTP errors")
    .labelNames(labels: _*)
    .register(underlying)

  override lazy val duration: Timer = {
    val name = "responses_duration_seconds"
    val help = "HTTP response duration"

    settings.durationConfig match {
      case Quantiles(qs, maxAge, ageBuckets) =>
        io.prometheus.client.Summary
          .build()
          .namespace(settings.namespace)
          .name(name)
          .help(help)
          .labelNames(labels: _*)
          .quantiles(qs: _*)
          .maxAgeSeconds(maxAge.toSeconds)
          .ageBuckets(ageBuckets)
          .register(underlying)
      case Buckets(bs) =>
        io.prometheus.client.Histogram
          .build()
          .namespace(settings.namespace)
          .name(name)
          .help(help)
          .labelNames(labels: _*)
          .buckets(bs: _*)
          .register(underlying)
    }
  }

  override lazy val sentBytes: Histogram = {
    val name = "responses_size_bytes"
    val help = "HTTP response size"

    settings.sentBytesConfig match {
      case Quantiles(qs, maxAge, ageBuckets) =>
        io.prometheus.client.Summary
          .build()
          .namespace(settings.namespace)
          .name(name)
          .help(help)
          .labelNames(labels: _*)
          .quantiles(qs: _*)
          .maxAgeSeconds(maxAge.toSeconds)
          .ageBuckets(ageBuckets)
          .register(underlying)

      case Buckets(bs) =>
        io.prometheus.client.Histogram
          .build()
          .namespace(settings.namespace)
          .name(name)
          .help(help)
          .labelNames(labels: _*)
          .buckets(bs: _*)
          .register(underlying)
    }
  }

  override val connected: Gauge = io.prometheus.client.Gauge
    .build()
    .namespace(settings.namespace)
    .name("connections_active")
    .help("Active TCP connections")
    .register(underlying)

  override val connections: Counter = io.prometheus.client.Counter
    .build()
    .namespace(settings.namespace)
    .name("connections_total")
    .help("Total TCP connections")
    .register(underlying)

  private def pathLabel(response: HttpResponse): String = {
    response.header[PathLabelHeaderCustom].getOrElse(PathLabelHeaderCustom.UnLabelled).value
  }

  private def userIdLabel(response: HttpResponse): String = {
    response.header[UserIdLabelHeader].getOrElse(UserIdLabelHeader.UnKnown).value
  }

  override def onRequest(request: HttpRequest, response: Future[HttpResponse])(
    implicit executionContext: ExecutionContext
  ): Unit = {
    active.inc()
    requests.inc()
    receivedBytes.update(request.entity.contentLengthOption.getOrElse(0L))
    val start = Deadline.now

    response.foreach { r =>
      // compute dimensions
      // format: off
      val methodDim = if (settings.includeMethodDimension) Some(MethodDimension(request.method)) else None
      val pathDim = if (settings.includePathDimension) Some(PathDimension(pathLabel(r))) else None
      val statusGroupDim = if (settings.includeStatusDimension) Some(StatusGroupDimension(r.status)) else None
      val userIdDim = Some(UserIdDimension(userIdLabel(r)))
      val dimensions = (methodDim ++ pathDim ++ statusGroupDim++ userIdDim).toSeq
      // format: on

      active.dec()
      responses.inc(dimensions)
      duration.observe(Deadline.now - start, dimensions)
      if (settings.defineError(r)) {
        errors.inc(dimensions)
      }
      r.entity.contentLengthOption.foreach(sentBytes.update(_, dimensions))
    }
  }
}

final case class UserIdDimension(value: String) extends Dimension {
  override def key: String = "userId"
}
