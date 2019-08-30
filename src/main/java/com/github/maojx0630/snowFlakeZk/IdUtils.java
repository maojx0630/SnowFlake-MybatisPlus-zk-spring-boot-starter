package com.github.maojx0630.snowFlakeZk;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 *
 * @author MaoJiaXing
 * @date 2019-08-29 13:44 
 */
public final class IdUtils {

	private static final Logger log= LoggerFactory.getLogger(IdUtils.class);

	/**
	 * 2010-11-04 09:42:54
	 * 时间起始标记点，作为基准，一般取系统的最近时间（一旦确定不能变动）
	 */
	private static final long benchmark = 1288834974657L;
	/**
	 * 机器标识位数
	 */
	private static final long workerIdBits = 5L;
	private static final long dataCenterIdBits = 5L;
	/**
	 * 毫秒内自增位
	 */
	private static final long sequenceBits = 12L;
	private static final long workerIdShift = sequenceBits;
	private static final long dataCenterIdShift = sequenceBits + workerIdBits;
	/**
	 * 时间戳左移动位
	 */
	private static final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;


	/**
	 * 根据Snowflake的ID，获取机器id
	 *
	 * @param id snowflake算法生成的id
	 * @return 所属机器的id
	 */
	public static long getWorkerId(long id) {
		return id >> workerIdShift & ~(-1L << workerIdBits);
	}

	/**
	 * 根据Snowflake的ID，获取数据中心id
	 *
	 * @param id snowflake算法生成的id
	 * @return 所属数据中心
	 */
	public static long getDataCenterId(long id) {
		return id >> dataCenterIdShift & ~(-1L << dataCenterIdBits);
	}

	/**
	 *根据Snowflake的ID，获取生成时间
	 *
	 * @param id snowflake算法生成的id
	 * @return 生成的时间
	 */
	public static long getTime(long id) {
		return (id >> timestampLeftShift & ~(-1L << 41L)) + benchmark;
	}

	/**
	 *根据Snowflake的ID，获取生成时间
	 *
	 * @param id snowflake算法生成的id
	 * @return 生成的时间
	 */
	public static Date getDate(long id) {
		return new Date(getTime(id));
	}

	public static Long next() {
		return IdWorker.getId();
	}

	public static String nextStr() {
		return IdWorker.getIdStr();
	}

	public static void initSequence(long workerId, long dataCenterId){
		IdWorker.initSequence(workerId,dataCenterId);
		log.info("IdWorker initSequence成功,当前workerId[{}],dataCenterId[{}]",workerId,dataCenterId);
	}
}
