package com.justafewmistakes.nim.gateway.kit;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Duty: spring对非IOC容器内的类对象 进行注入获取IOC容器内东西(Bean)的工厂
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public final class SpringBeanFactory implements ApplicationContextAware {
    private static ApplicationContext context;

    public static <T> T getBean(Class<T> c){
        return context.getBean(c);
    }


    public static <T> T getBean(String name,Class<T> clazz){
        return context.getBean(name,clazz);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }


}