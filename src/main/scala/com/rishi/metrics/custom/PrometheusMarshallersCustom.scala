package com.rishi.metrics.custom

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.{ContentType, HttpCharsets, HttpEntity, MediaTypes}
import io.prometheus.client.exporter.common.TextFormat

import java.io.StringWriter

trait PrometheusMarshallersCustom {

  val PrometheusContentType: ContentType = {
    MediaTypes.`text/plain` withParams Map("version" -> "0.0.4") withCharset HttpCharsets.`UTF-8`
  }

  implicit val marshaller: ToEntityMarshaller[PrometheusRegistryCustom] = {
    Marshaller.opaque { registry =>
      val output = new StringWriter()
      try {
        TextFormat.write004(output, registry.underlying.metricFamilySamples)
        HttpEntity(output.toString).withContentType(PrometheusContentType)
      } finally {
        output.close()
      }
    }
  }
}

object PrometheusMarshallersCustom extends PrometheusMarshallersCustom
