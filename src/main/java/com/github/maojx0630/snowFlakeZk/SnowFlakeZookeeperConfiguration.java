package com.github.maojx0630.snowFlakeZk;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 *
 * @author MaoJiaXing
 * @date 2019-08-29 14:43 
 */
public class SnowFlakeZookeeperConfiguration {

	@Bean
	@ConditionalOnClass(IdWorker.class)
	public SnowFlakeZkApplicationRunner snowFlakeZkApplicationRunner(ZookeeperConfig config) {
		return new SnowFlakeZkApplicationRunner(config);
	}

}
