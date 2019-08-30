package com.github.maojx0630.snowFlakeZk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 *
 * @author MaoJiaXing
 * @date 2019-08-28 14:51
 */
public class SnowFlakeZookeeper {

	private static final Logger log = LoggerFactory.getLogger(SnowFlakeZookeeper.class);

	private CuratorFramework client;

	private ZookeeperConfig config;

	private long workerId = 0L;

	private long dataCenterId = 0L;

	private String uuid;

	public SnowFlakeZookeeper(ZookeeperConfig config) throws Exception {
		//探测zk是否启动
		try {
			String[] array = config.getConnect().split(":");
			Socket socket = new Socket(array[0], Integer.valueOf(array[1]));
			socket.close();
		} catch (Exception e) {
			workerId=config.getDefaultWorkerId();
			dataCenterId=config.getDefaultDataCenterId();
			IdUtils.initSequence(workerId,dataCenterId);
			log.error("zk连接失败,使用默认参数初始化,若非集群模式请取消zk获取改为固定值,目前workerID为[{}],dataCenterId为[{}]", workerId, dataCenterId);
			return;
		}
		//初始化zk操作工具
		this.config = config;
		client = CuratorFrameworkFactory.builder().
				connectString(config.getConnect()).
				sessionTimeoutMs(config.getSessionTimeoutMs()).
				connectionTimeoutMs(config.getConnectionTimeoutMs()).
				retryPolicy(new RetryNTimes(config.getRetryCount(), config.getRetryInterval())).
				build();
		//开启操作zk node
		client.start();
		try {
			//创建root节点
			client.create().withMode(CreateMode.PERSISTENT).forPath(config.getRoot());
		} catch (Exception e) {
			//若节点已存在则忽略,不存在则抛出异常
			if (!(e instanceof KeeperException.NodeExistsException)) {
				throw e;
			}
		}
		//初始化集群与机器id
		initWorkIdAndCenterId();
		IdUtils.initSequence(workerId,dataCenterId);
		//添加连接监听,处理zk断开的意外情况
		client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> {
			if (connectionState.isConnected()) {
				try {
					String path = config.getRoot() + "/" + dataCenterId + "-" + workerId;
					String str = new String(client.getData().forPath(path), StandardCharsets.UTF_8);
					if (str.equals(uuid)) {
						try {
							client.delete().forPath(path);
							log.info("zk连接已恢复,zk存储信息比对成功,即将移除并重新注册");
						} catch (Exception ignored) {
						}
					}
					client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
					log.info("zk连接已恢复并重新将当前信息注册,目前workerID为[{}],dataCenterId为[{}]", workerId, dataCenterId);
				} catch (KeeperException.NodeExistsException | KeeperException.NoNodeException exception) {
					try {
						log.info("zk连接已恢复,但当前节点信息已被占用,或已被清除将重新注册");
						workerId = 0;
						dataCenterId = 0;
						initWorkIdAndCenterId();
						IdUtils.initSequence(workerId,dataCenterId);
						log.info("重新注册,并重新初始化雪花信息成功,目前workerID为[{}],dataCenterId为[{}]", workerId, dataCenterId);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				log.error("zk连接已断开!!!!");
				log.error("zk连接已断开!!!!");
				log.error("zk连接已断开!!!!");
			}
		});
	}

	private void initWorkIdAndCenterId() throws Exception {
		long l = 0;
		while (true) {
			try {
				//创建子节点
				String path = config.getRoot() + "/" + dataCenterId + "-" + l;
				client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
				uuid = UUID.randomUUID().toString();
				client.setData().forPath(path, uuid.getBytes(StandardCharsets.UTF_8));
			} catch (KeeperException.NodeExistsException exception) {
				//创建失败重试,若机器id 为31则重置为0增加集群id
				if (l == 31) {
					dataCenterId++;
					//若集群id超过31则没有可用的集群机器id可用
					if (dataCenterId > 31) {
						throw new RuntimeException("超出最大id生成规则,不能启动!");
					}
					workerId = 0L;
				} else {
					l++;
				}
				continue;
			}
			workerId = l;
			break;
		}
	}
}
