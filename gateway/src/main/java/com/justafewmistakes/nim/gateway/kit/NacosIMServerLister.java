package com.justafewmistakes.nim.gateway.kit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Duty: 用于监听IMServer的实时数据，与现有连接对比后，进行重连或断开
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class NacosIMServerLister implements Runnable{
    private final static Logger LOGGER = LoggerFactory.getLogger(NacosIMServerLister.class);

    private final NacosClient client; //不能自动注入，这个可能是因为它还没加入IOC创建就开始run了，来不及注入(CommandLineRunner)

    public NacosIMServerLister() {
        client = SpringBeanFactory.getBean(NacosClient.class);
    }

    @Override
    public void run() {
        client.subscribe();
    }
}
