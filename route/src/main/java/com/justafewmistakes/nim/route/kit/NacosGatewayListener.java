package com.justafewmistakes.nim.route.kit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Duty:
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
public class NacosGatewayListener implements Runnable{

    private final static Logger LOGGER = LoggerFactory.getLogger(NacosGatewayListener.class);

    private final NacosClient client; //不能自动注入，这个可能是因为它还没加入IOC创建就开始run了，来不及注入(CommandLineRunner)

    public NacosGatewayListener() {
        client = SpringBeanFactory.getBean(NacosClient.class);
    }

    @Override
    public void run() {
        client.subscribe();

    }
}
