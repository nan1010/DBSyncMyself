package dbsync.task;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import dbsync.entity.Constants;
import dbsync.entity.DBInfo;
import dbsync.entity.DBSyncException;
import dbsync.entity.JobInfo;
import dbsync.utils.StringUtils;
import dbsync.utils.Tool;

/**
 * @author zhaonan
 * @date 2020/9/1 15:30
 * @description 初始化、数据同步、源数据删除
 * @version 1.0.0
 */

public class Task {

	private DBInfo srcDb;
	private DBInfo destDb;
	private List<JobInfo> jobList;
	private String code;
	private static Logger logger = Logger.getLogger(Task.class);

	private Task() {
	}

	/**
	 * 创建DBSyncBuilder对象
	 * 
	 * @return DBSyncBuilder对象
	 */
	public static Task builder() {
		return new Task();
	}

	/**
	 * 初始化数据库信息并解析jobs.xml填充数据
	 * 
	 * @return DBSyncBuilder对象
	 */
	public Task init() {
		srcDb = new DBInfo();
		destDb = new DBInfo();
		jobList = new ArrayList<JobInfo>();
		SAXReader reader = new SAXReader();
		try {
			// 读取xml的配置文件名，并获取其里面的节点
			Element root = reader.read("jobs.xml").getRootElement();
			Element src = root.element("source");
			Element dest = root.element("dest");
			Element jobs = root.element("jobs");
			// 遍历job即同步的表
			for (@SuppressWarnings("rawtypes")
			Iterator it = jobs.elementIterator("job"); it.hasNext();) {
				jobList.add((JobInfo) elementInObject((Element) it.next(), new JobInfo()));
			}
			//
			elementInObject(src, srcDb);
			elementInObject(dest, destDb);
			code = root.element("code").getTextTrim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	/**
	 * 解析e中的元素，将数据填充到o中
	 * 
	 * @param e 解析的XML Element对象
	 * @param o 存放解析后的XML Element对象
	 * @return 存放有解析后数据的Object
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public Object elementInObject(Element e, Object o) throws IllegalArgumentException, IllegalAccessException {
		Field[] fields = o.getClass().getDeclaredFields();
		for (int index = 0; index < fields.length; index++) {
			Field item = fields[index];
			// 当前字段不是serialVersionUID，同时当前字段不包含serialVersionUID
			if (!Constants.FIELD_SERIALVERSIONUID.equals(item.getName())
					&& !item.getName().contains(Constants.FIELD_SERIALVERSIONUID)) {
				item.setAccessible(true);
				item.set(o, e.element(item.getName()).getTextTrim());
			}
		}
		return o;
	}

	/**
	 * 执行数据同步，成功后删除源数据
	 */
	public void start() {
		for (int index = 0; index < jobList.size();) {
			JobInfo jobInfo = jobList.get(index);
			String logTitle = "[" + code + "]" + jobInfo.getName() + " ";
			logger.info(jobInfo.getCron());
			// 每隔一秒循环执行
			while (true) {
				try {
					Thread.sleep(1000);
					Task.logger.info("开始任务执行: ");
					Connection inConn = null;
					Connection outConn = null;
					try {
						inConn = createConnection(srcDb);
						outConn = createConnection(destDb);
						if (inConn == null) {
							Task.logger.info("请检查源数据连接!");
							return;
						} else if (outConn == null) {
							Task.logger.info("请检查目标数据连接!");
							return;
						}
						long start = System.currentTimeMillis();
						assembleAndExcuteSQLAndDelete(inConn, outConn, jobInfo);
						Task.logger.info("执行耗时: " + (System.currentTimeMillis() - start) + "ms");
					} catch (SQLException e) {
						Task.logger.error(logTitle + e.getMessage());
						Task.logger.error(logTitle + " SQL执行出错，请检查是否存在语法错误");
						throw new DBSyncException(logTitle + e.getMessage());
					} finally {
						Task.logger.info("关闭源数据库连接");
						destoryConnection(inConn);
						Task.logger.info("关闭目标数据库连接");
						destoryConnection(outConn);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
			Task.logger.error(e.getMessage());
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
				Task.logger.error("连接已关闭");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 拼凑SQL语句，执行同步后删除源数据
	public String assembleAndExcuteSQLAndDelete(Connection inConn, Connection outConn, JobInfo jobInfo)
			throws SQLException {
		String uniqueName = Tool.generateString(6) + "_" + jobInfo.getName();
		String[] destFields = jobInfo.getDestTableFields().split(Constants.FIELD_SPLIT);
		destFields = this.trimArrayItem(destFields);
		// 默认的srcFields数组与destFields相同
		String[] srcFields = destFields;
		String srcField = jobInfo.getSrcTableFields();
		if (!StringUtils.isEmpty(srcField)) {
			srcFields = this.trimArrayItem(srcField.split(Constants.FIELD_SPLIT));
		}
		Map<String, String> fieldMapper = this.getFieldsMapper(srcFields, destFields);
		String[] updateFields = jobInfo.getDestTableUpdate().split(Constants.FIELD_SPLIT);
		updateFields = this.trimArrayItem(updateFields);
		String destTable = jobInfo.getDestTable();
		String destTableKey = jobInfo.getDestTableKey();

		PreparedStatement pst = inConn.prepareStatement(jobInfo.getSrcSql());
		ResultSet rs = pst.executeQuery();
		StringBuilder sql = new StringBuilder();
		sql.append("insert into ").append(destTable).append(" (").append(jobInfo.getDestTableFields())
				.append(") values ");
		boolean flag = true;
		long count = 0;
		for (int i = 0; i < 40; i++) {
			if (!rs.next()) {
				flag = false;
				break;
			}
			sql.append("(");
			for (int index = 0; index < destFields.length; index++) {
				Object fieldValue = rs.getObject(fieldMapper.get(destFields[index].trim()));
				if (fieldValue == null) {
					sql.append(fieldValue).append(index == (destFields.length - 1) ? "" : ",");
				} else {
					sql.append("'").append(fieldValue).append(index == (destFields.length - 1) ? "'" : "',");
				}
			}
			sql.append("),");
			// 新插入一个字段的sql，计数+1
			count++;
		}
		if (count > 0) {
			sql = sql.deleteCharAt(sql.length() - 1);
			if ((!StringUtils.isEmpty(jobInfo.getDestTableUpdate()))
					&& (!StringUtils.isEmpty(jobInfo.getDestTableKey()))) {
				sql.append(" on duplicate key update ");
				for (int index = 0; index < updateFields.length; index++) {
					sql.append(updateFields[index]).append("= values(").append(updateFields[index])
							.append(index == (updateFields.length - 1) ? ")" : "),");
				}
				String newSql = new StringBuffer("alter table ").append(destTable).append(" add constraint ")
						.append(uniqueName).append(" unique (").append(destTableKey).append(");").append(sql.toString())
						.append(";alter table ").append(destTable).append(" drop index ").append(uniqueName).toString();
				executeAndDeleteSQL(newSql, inConn, outConn, flag);
				if (!rs.next()) {
					pst.close();
					rs.close();
					return newSql;
				}
			}
			logger.debug(sql.toString());
		}
		return destTableKey;
	}

	// 执行SQL语句，删除源数据
	public void executeAndDeleteSQL(String sql, Connection inConn, Connection outConn, boolean flag)
			throws SQLException {
		PreparedStatement pst1 = outConn.prepareStatement("");
		PreparedStatement pst2 = outConn.prepareStatement("select id from t_user");
		Statement statement = inConn.createStatement();
		StringBuilder delSql = new StringBuilder();
		String[] sqlList = sql.split(";");
		for (int index = 0; index < sqlList.length; index++) {
			pst1.addBatch(sqlList[index]);
		}
		pst1.executeBatch();
		ResultSet resultSet = pst2.executeQuery();

		while (resultSet.next()) {
			delSql.append(resultSet.getInt("id")).append(",");
		}
		delSql = delSql.deleteCharAt(delSql.length() - 1);
		if (!flag) {
			statement.executeUpdate("delete from t_user");
			Task.logger.info("源数据全部删除完毕！");
		} else {

			statement.executeUpdate("delete from t_user where id in " + "(" + delSql + ")");
			Task.logger.info("delete from t_user where id in " + "(" + delSql + ")");
		}
		statement.close();
		outConn.commit();
		inConn.commit();
		pst1.close();
	}

	/**
	 * 去除String数组每个元素中的空格
	 * 
	 * @param src 需要去除空格的数组
	 * @return 去除空格后的数组
	 */
	private String[] trimArrayItem(String[] src) {
		if (src == null || src.length == 0)
			return src;
		String[] dest = new String[src.length];
		for (int i = 0; i < src.length; i++) {
			dest[i] = src[i].trim();
		}
		return dest;
	}

	/**
	 * 构建字段的映射关系
	 */
	private Map<String, String> getFieldsMapper(String[] srcFields, String[] destFields) {
		if (srcFields.length != destFields.length) {
			throw new DBSyncException("源数据库与目标数据库的字段必须一一对应");
		}
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < srcFields.length; i++) {
			map.put(destFields[i].trim(), srcFields[i].trim());
		}
		return map;
	}
}
