package cn.xbed.rpc

import java.util.UUID

import akka.actor.{Actor, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import scala.concurrent.duration._

/**
  * Created by root on 11/12/17.
  */
class Worker(val masterHost: String, val masterPort : Int, val memory: Int, val cores : Int) extends Actor{

  var master : ActorSelection = _
  val workerId = UUID.randomUUID().toString
  val HEARTBEAT_INTERVAL = 10000

  override def preStart() : Unit = {
    // connect with master
    master = context.actorSelection(s"akka.tcp://MasterSystem@$masterHost:$masterPort/user/Master")
    // send register massage to master
    master ! RegisterWorker(workerId, memory, cores)
  }


  override def receive: Receive = {
    case RegisteredWorker(masterUrl) => {
      // print masterurl
      println(masterUrl)
      // import implict
      import context.dispatcher
      // send to self
      context.system.scheduler.schedule(0 millis, HEARTBEAT_INTERVAL millis, self, SendHeartbeat)
    }

    // send heartbeat to master
    case SendHeartbeat => {
      // TODO
      println("send heartbeat to master, workerId:" + workerId)
      // send heartbeat
      master ! Heartbeat(workerId)
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
    val memory = args(4).toInt
    val cores = args(5).toInt
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
    val worker = actorSystem.actorOf(Props(new Worker(masterHost, masterPort, memory, cores)), "Worker")
    // exit
    actorSystem.awaitTermination()
  }
}
