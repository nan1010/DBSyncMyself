package dbsync;

import java.util.Date;
import org.apache.log4j.Logger;

import dbsync.task.Task;
import dbsync.utils.DateUtils;

/**
 * @author 赵楠
 * @date 2020/8/31 10:07
 * @description 程序入口
 * @version 1.0.0
 */
public class Main {

	private static Logger logger = Logger.getLogger(Task.class);

	public static void main(String[] args) {
		logger.info("同步数据开始===>>>" + DateUtils.parseDateToString(new Date(), DateUtils.DATE_TIME_FORMAT));
		// System.out.println("1333");
		Task.builder().init().start();
		logger.info("同步数据结束===>>>" + DateUtils.parseDateToString(new Date(), DateUtils.DATE_TIME_FORMAT));
		// System.out.println("1222222");
	}
}
