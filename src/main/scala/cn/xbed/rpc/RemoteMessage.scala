package cn.xbed.rpc

/**
  * Created by root on 11/12/17.
  */
trait RemoteMessage extends Serializable

// worker -> master
case class RegisterWorker(id : String, memory : Int, cores : Int) extends RemoteMessage
case class Heartbeat(id : String)extends RemoteMessage

// master -> worker
case class RegisteredWorker(masterUrl : String) extends RemoteMessage

// worker -> self
case object SendHeartbeat

// master -> self
case object CheckTimeOutWorker
