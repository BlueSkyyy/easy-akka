package cn.xbed.rpc

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

/**
  * Created by root on 11/12/17.
  */
class Worker(val masterHost: String, val masterPort : Int ) extends Actor{

  var master : ActorSelection = _


  override def preStart() : Unit = {
    // connect with master
    master = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")
    // send register massage to master
    master ! "connected"


  }


  override def receive: Receive = {
    case "reply" => {
      println("a reply from master")
    }

  }
}

object Worker{
  def main(args: Array[String]): Unit = {
    // defined host and port
    val host = args(0)
    val port = args(1).toInt
    val masterHost = args(2)
    val masterPort = args(3).toInt
    // create config
    val configStr =
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
      """.stripMargin
    val config = ConfigFactory.parseString(configStr)
    // create and monitor actor, singleton
    val actorSystem = ActorSystem("WorkerSystem", config)
    // create actor
    val worker = actorSystem.actorOf(Props(new Worker(masterHost, masterPort)), "Worker")
    // exit
    actorSystem.awaitTermination()
  }
}
