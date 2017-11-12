package cn.xbed.rpc

import akka.actor.{Actor, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import scala.collection.mutable
import scala.concurrent.duration._


/**
  * Created by root on 11/12/17.
  */
class Master(val host : String, val port : Int) extends Actor{

  // workerId -> worker
  val idToWorker = new mutable.HashMap[String, WorkerInfo]()
  // workerInfoSet
  val workerInfoSet = new mutable.HashSet[WorkerInfo]()
  // timeout check interval
  val CHECK_INTERVAL = 15000

  override def preStart(): Unit = {
    println("preStart invoked")
    // import implict
    import context.dispatcher
    context.system.scheduler.schedule(0 millis, CHECK_INTERVAL millis, self, CheckTimeOutWorker)
  }

  // receive message
  override def receive: Receive = {

    case RegisterWorker(id, memory, cores) => {
      if(!idToWorker.contains(id)){
        val workerInfo = new WorkerInfo(id, memory, cores)
        idToWorker(id) = workerInfo
        workerInfoSet += workerInfo
        sender() ! RegisteredWorker(s"akka.tcp://MasterSystem@$host:$port/user/Master")
      }
    }

    case Heartbeat(id) => {
      // response to worker
      if(idToWorker.contains(id)){
        // get workerInfo
        val workerInfo = idToWorker(id)
        // active
        val currentTime = System.currentTimeMillis()
        workerInfo.lastHeartbeatTime = currentTime
      }
    }

    case CheckTimeOutWorker => {
      val currentTime = System.currentTimeMillis()
      // kill timeout worker
      val toRemove = workerInfoSet.filter(x => currentTime - x.lastHeartbeatTime > CHECK_INTERVAL)
      for (w <- toRemove){
        workerInfoSet -= w
        idToWorker -= w.id
      }
      println("active worker number:" +workerInfoSet.size)
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
    val master = actorSystem.actorOf(Props(new Master(host, port)), "Master")
    // exit
    actorSystem.awaitTermination()
  }

}
