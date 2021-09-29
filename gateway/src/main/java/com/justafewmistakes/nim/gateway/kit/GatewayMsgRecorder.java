package com.justafewmistakes.nim.gateway.kit;

import com.justafewmistakes.nim.common.kit.MsgRecorder;
import com.justafewmistakes.nim.gateway.config.AppConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Duty: 网关对消息进行异步记录的记录器（在网关端仅对离线数据进行记录）
 *
 * @author justafewmistakes
 * Date: 2021/09
 */
@Component
public class GatewayMsgRecorder implements MsgRecorder {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayMsgRecorder.class);

    private BlockingQueue<String[]> blockingQueue; //阻塞队列，用于缓存要记录的消息

    private final AtomicBoolean isStarted = new AtomicBoolean(false); //是否开始记录了

    private Recorder recorder; //异步记录消息的线程

    @Value("${nim.filePath}")
    private String filePath; //文件记录地址

    @Autowired
    private RedisTemplate<String, String> redisTemplate; //操作redis

    @Autowired
    private AppConfiguration appConfiguration;

    @Override
    public void record(String preDestination, String msg) {
        startRecorder();
        try {
            blockingQueue.put(new String[]{preDestination, msg});
        } catch (InterruptedException e) {
            LOGGER.error("向异步记录队列中put数据时被打断,异常消息为:",e);
        }
    }

    @Override
    public void stop() {
        LOGGER.info("关闭异步记录器");
        isStarted.set(false);
        recorder.interrupt();
    }

    /**
     * 在用户登入的时候调用一次
     * 搜索是否有用户的离线消息，preDestination是用户端的id与前缀
     * 如果有返回离线消息的文件名列表
     * TODO：判断直接从redis中判断来获取
     */
    public List<String> searchOfflineFileName(String preDestination) {
        //先从redis中检查是否有该用户端的离线数据
        ArrayList<String> list = new ArrayList<>();
        String gateway = redisTemplate.opsForValue().get(preDestination);
        if(!(appConfiguration.getGatewayIp() + ":" + appConfiguration.getGatewayPort()).equals(gateway)) return list;

        String destination = preDestination.split(":")[1];// 0是前缀

        // 获取文件名
        Path dir = Paths.get(filePath + "/" + destination + "/");//存储该用户离线文件的位置
        try {
            Stream<Path> stream = Files.list(dir);
            List<Path> paths = stream.collect(Collectors.toList());
            for(Path path : paths) list.add(path.toString()); //FIXME：不知道这里返回的是否是正确的路径
        } catch (IOException e) {
            LOGGER.error("io错误，无法获取用于记录[{}]文件的离线文件列表", destination);
        }
        return list;
    }


    // TODO：搜索未完成
    @Override
    public String search(String key) {

        return null;
    }

    /**
     * 异步工作线程
     */
    private class Recorder extends Thread {
        @Override
        public void run() {
            while (isStarted.get()) {
                try {
                    String[] tuple = blockingQueue.take();
                    doWriteAsync(tuple[0], tuple[1]);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    /**
     * 开启异步工作线程
     */
    private void startRecorder() {
        if(isStarted.get()) return;

        // 初始化
        blockingQueue = new ArrayBlockingQueue<>(appConfiguration.getQueSize());
        recorder = new Recorder();

        isStarted.compareAndSet(false, true);
        recorder.setDaemon(true);
        recorder.setName("网关异步记录线程");
        recorder.start();
    }

    /**
     * 写入操作
     * preDestination是离线的客户端的id与前缀
     * TODO:要想redis中也写入元数据
     */
    private void doWriteAsync(String preDestination, String msg) {
        LOGGER.info("正在异步进行记录该数据：[{}]", msg);
        LocalDate now = LocalDate.now();
        int year = now.getYear(), month = now.getMonthValue(), day = now.getDayOfMonth();

        String destination = preDestination.split(":")[1];

        String dir = filePath + "/" + destination + "/";
        String fileName = dir + year + month + day + ".log"; //层级为 基本路径/目的地/日期.log

        Path file = Paths.get(fileName);
        boolean isExists = Files.exists(Paths.get(dir));
        try {
            if(!isExists) Files.createDirectories(Paths.get(dir)); //不存在则创建目录
            else { //如果存在大于1份,就清除
                Stream<Path> list = Files.list(Paths.get(dir));
                List<Path> paths = list.collect(Collectors.toList());
                if(!Files.exists(file) && paths.size() > 0) {
                    Files.delete(paths.get(0));
                }
            }

            // 将离线客户端的id和记录他离线消息的网关联系起来放入redis
            redisTemplate.opsForValue().set(preDestination, appConfiguration.getGatewayIp() + ":" + appConfiguration.getGatewayPort());
            // 写入本地文件
            Files.write(file, Collections.singletonList(msg), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.error("io错误，无法创建文件用于记录[{}]的离线消息[{}]", destination, msg);
        }
    }
}
