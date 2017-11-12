package cn.xbed.rpc

/**
  * Created by root on 11/12/17.
  */
class WorkerInfo(val id : String, val memory : Int, val cores : Int) {
  // record heartbeat time
  var lastHeartbeatTime : Long = _
}
