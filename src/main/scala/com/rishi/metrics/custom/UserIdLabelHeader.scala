package com.rishi.metrics.custom

import akka.http.scaladsl.model.headers.{ModeledCustomHeader, ModeledCustomHeaderCompanion}

import scala.util.{Success, Try}

final case class UserIdLabelHeader(value: String) extends ModeledCustomHeader[UserIdLabelHeader] {
  override def renderInRequests = false

  override def renderInResponses = false

  override val companion = UserIdLabelHeader
}

object UserIdLabelHeader extends ModeledCustomHeaderCompanion[UserIdLabelHeader] {

  val UnKnown: UserIdLabelHeader = UserIdLabelHeader("UnKnown")

  override val name = "x-userid-label"

  override def parse(value: String): Try[UserIdLabelHeader] = Success(new UserIdLabelHeader(value))
}
