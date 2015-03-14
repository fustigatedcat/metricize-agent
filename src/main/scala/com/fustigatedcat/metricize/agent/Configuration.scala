package com.fustigatedcat.metricize.agent

import java.io.File

import com.typesafe.config.ConfigFactory

object Configuration {
  val config = ConfigFactory.load()
  object api {
    val baseurl = config.getString("api.baseurl")
  }

  object keyfile {
    val customer = config.getString("keyfile.customer")
    val agent = config.getString("keyfile.agent")
  }

  object agent {
    object conf {
      val reload = config.getInt("agent.conf.reload")
      val file = config.getString("agent.conf.file")
    }
  }

  lazy val agentConf = {
    val file = new File(Configuration.agent.conf.file)
    if(file == null || !file.exists()) {
      None
    } else {
      Some(ConfigFactory.parseFile(file))
    }
  }

}
