package com.fustigatedcat.metricize.agent

import com.typesafe.config.ConfigFactory

object Configuration {
  val config = ConfigFactory.load()
  object api {
    val baseurl = config.getString("api.baseurl")
  }

}
