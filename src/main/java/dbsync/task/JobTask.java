/**
 * 
 */
package dbsync.task;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dbsync.MySQLSync.MySQLSync;
import dbsync.constants.Constants;
import dbsync.entity.DBInfo;
import dbsync.entity.JobInfo;
import dbsync.exception.DBSyncException;
import dbsync.utils.DateUtils;

/**
 * @author 0380009503
 *
 */
public class JobTask {
	private final Logger logger = LoggerFactory.getLogger(JobTask.class);
	private HashMap<String, Object> jobDataMap;

	/**
	 * <p>
	 * Set the <code>JobDataMap</code> to be associated with the <code>Job</code>.
	 * </p>
	 */
	public void setJobDataMap(HashMap<String, Object> jobDataMap) {
		this.jobDataMap = jobDataMap;
	}

	/**
	 * @return
	 */
	public HashMap<String, Object> getJobDataMap() {
		if (jobDataMap == null) {
			jobDataMap = new HashMap<String, Object>();
		}
		return jobDataMap;
	}

	/**
	 * 
	 */
	public void run() {

		/**
		 * 执行同步数据库任务
		 *
		 */
		this.logger.info("开始任务执行: {}", DateUtils.parseDateToString(new Date(), DateUtils.DATE_TIME_FORMAT));
		Connection inConn = null;
		Connection outConn = null;
		DBInfo srcDb = (DBInfo) jobDataMap.get(Constants.SRC_DB);
		DBInfo destDb = (DBInfo) jobDataMap.get(Constants.DEST_DB);
		JobInfo jobInfo = (JobInfo) jobDataMap.get(Constants.JOB_INFO);
		String logTitle = (String) jobDataMap.get(Constants.LOG_TITLE);
		try {
			inConn = createConnection(srcDb);
			outConn = createConnection(destDb);
			if (inConn == null) {
				this.logger.info("请检查源数据连接!");
				return;
			} else if (outConn == null) {
				this.logger.info("请检查目标数据连接!");
				return;
			}
			MySQLSync mySQLSync = new MySQLSync();
			long start = System.currentTimeMillis();
			mySQLSync.assembleSQL(jobInfo.getSrcSql(), inConn, outConn, jobInfo);
			this.logger.info("执行耗时: " + (System.currentTimeMillis() - start) + "ms");
		} catch (SQLException e) {
			this.logger.error(logTitle + e.getMessage());
			this.logger.error(logTitle + " SQL执行出错，请检查是否存在语法错误");
			throw new DBSyncException(logTitle + e.getMessage());
		} finally {
			this.logger.info("关闭源数据库连接");
			destoryConnection(inConn);
			this.logger.info("关闭目标数据库连接");
			destoryConnection(outConn);
		}
	}

	/**
	 * 创建数据库连接
	 * 
	 * @param db
	 * @return
	 */
	private Connection createConnection(DBInfo db) {
		try {
			Class.forName(db.getDriver());
			Connection conn = DriverManager.getConnection(db.getUrl(), db.getUsername(), db.getPassword());
			conn.setAutoCommit(false);
			return conn;
		} catch (Exception e) {
			this.logger.error(e.getMessage());
		}
		return null;
	}

	/**
	 * 关闭并销毁数据库连接
	 * 
	 * @param conn
	 */
	private void destoryConnection(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
				conn = null;
				this.logger.error("数据库连接关闭");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
