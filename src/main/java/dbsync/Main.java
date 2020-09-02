package dbsync;

import java.text.DateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import dbsync.task.Task;

/**
 * @author 赵楠
 * @date 2020/8/31 10:07
 * @description 程序入口
 * @version 1.0.0
 */
public class Main {
	private static Logger logger = Logger.getLogger(Task.class);

	public static void main(String[] args) {
		logger.info("同步数据开始===>>>" + DateFormat.getDateInstance(0).format(new Date()).toString());
		Task.builder().init().start();
		logger.info("同步数据结束===>>>" + DateFormat.getDateInstance(0).format(new Date()).toString());	}
}
