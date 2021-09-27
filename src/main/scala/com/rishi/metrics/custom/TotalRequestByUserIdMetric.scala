package com.rishi.metrics.custom

import com.rishi.metrics.controller.MetricsController.{collector, settings}
import fr.davit.akka.http.metrics.core.Dimension
import io.prometheus.client.Counter

object TotalRequestByUserIdMetric {
  private val userIdLabel: Seq[String] = Seq("userId")

  private val totalRequestCounter: Counter = Counter.build().
    namespace(settings.namespace)
    .name("request_by_user_id")
    .help("Total Request by User ID")
    .labelNames(userIdLabel: _*)
    .register(collector)

  def inc(dimensions: Seq[Dimension]): Unit = {
    totalRequestCounter.labels(dimensions.map(_.value): _*).inc()
  }
}

final case class UserIdDimension(userId: String) extends Dimension {
  override def key: String = "userId"

  override def value: String = userId
}
