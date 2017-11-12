package cn.xbed.rpc

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by root on 11/12/17.
  */
class Master extends Actor{

  println("constructor invoked")

  override def preStart(): Unit = {
    println("preStart invoked")
  }

  // receive message
  override def receive: Receive = {
    case "connected" => {
      println("a client connected")
      // reply to worker
      sender() ! "reply"
    }

    case "hello" => {
      println("hello akka" +
        "")
    }
  }
}


object Master {
  def main(args: Array[String]): Unit = {
    // defined host and port
    val host = args(0)
    val port = args(1).toInt
    // create config
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
      """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    // create and monitor actor, singleton
    val actorSystem = ActorSystem("MasterSystem", config)
    // create actor
    val master = actorSystem.actorOf(Props(new Master), "Master")
    //send massage
    master ! "hello"
    // exit
    actorSystem.awaitTermination()
  }

}
