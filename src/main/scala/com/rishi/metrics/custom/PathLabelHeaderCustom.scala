package com.rishi.metrics.custom

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.{Success, Try}

final case class PathLabelHeaderCustom(value: String) extends ModeledCustomHeader[PathLabelHeaderCustom] {
  override def renderInRequests = false

  override def renderInResponses = false

  override val companion = PathLabelHeaderCustom
}

object PathLabelHeaderCustom extends ModeledCustomHeaderCompanion[PathLabelHeaderCustom] {

  val Unhandled: PathLabelHeaderCustom = PathLabelHeaderCustom("unhandled")
  val UnLabelled: PathLabelHeaderCustom = PathLabelHeaderCustom("unlabelled")

  override val name = "x-path-label"

  override def parse(value: String): Try[PathLabelHeaderCustom] = Success(new PathLabelHeaderCustom(value))
}