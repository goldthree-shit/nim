```
      ___                       ___     
     /__/\        ___          /__/\    
     \  \:\      /  /\        |  |::\   
      \  \:\    /  /:/        |  |:|:\  
  _____\__\:\  /__/::\      __|__|:|\:\ 
 /__/::::::::\ \__\/\:\__  /__/::::| \:\
 \  \:\~~\~~\/    \  \:\/\ \  \:\~~\__\/
  \  \:\  ~~~      \__\::/  \  \:\      
   \  \:\          /__/:/    \  \:\     
    \  \:\         \__\/      \  \:\    
     \__\/                     \__\/    
```

###该包是通用模块中的路由协议包，用于客户端申请连接上网关时，通过使用路由协议获取可用的网关(网关连接所有的IM服务器，依次向IM服务器发送消息)
* hash： 使用一致性哈希算法的路由选择协议
* loop： 使用轮询算法的路由选择协议
* select： 选择负载最小，可用性最好的路由


