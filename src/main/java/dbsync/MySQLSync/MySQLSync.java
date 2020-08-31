/**
 * 
 */
package dbsync.MySQLSync;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dbsync.constants.Constants;
import dbsync.entity.JobInfo;
import dbsync.exception.DBSyncException;
import dbsync.utils.StringUtils;
import dbsync.utils.Tool;

public class MySQLSync {
	private Logger logger = LoggerFactory.getLogger(MySQLSync.class);
	private long count;
	private boolean flag = true;

	// srcSql參數好像不需要
	public String assembleSQL(String srcSql, Connection inConn, Connection outConn, JobInfo jobInfo)
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
		while (true) {
			PreparedStatement pst = inConn.prepareStatement(srcSql);
			ResultSet rs = pst.executeQuery();
			StringBuilder sql = new StringBuilder();
			sql.append("insert into ").append(destTable).append(" (").append(jobInfo.getDestTableFields())
					.append(") values ");
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
							.append(uniqueName).append(" unique (").append(destTableKey).append(");")
							.append(sql.toString()).append(";alter table ").append(destTable).append(" drop index ")
							.append(uniqueName).toString();
					executeSQLAndDelete(newSql, outConn);
					delConfig(inConn, rs, flag);
					if (!rs.next()) {
						pst.close();
						rs.close();
						return newSql;
					}
				}
				logger.debug(sql.toString());
			}
		}
	}

	// 执行SQL语句
	public void executeSQLAndDelete(String sql, Connection conn) throws SQLException {
		PreparedStatement pst = conn.prepareStatement("");
		String[] sqlList = sql.split(";");
		for (int index = 0; index < sqlList.length; index++) {
			pst.addBatch(sqlList[index]);
		}
		pst.executeBatch();
		conn.commit();
		pst.close();
	}

	/**
	 * @param inConn
	 * @param srcSql
	 */
	public void delConfig(Connection inConn, ResultSet rs, boolean flag) {
		try {
			Statement st = inConn.createStatement();
			if (!flag) {
				st.executeUpdate("delete from t_user");
				this.logger.info("源数据删除完毕！");
			} else {
				int id = rs.getInt("id");
				st.executeUpdate("delete from t_user where id <=" + id);
				this.logger.info("delete from t_user where id <=" + id);
			}
			st.close();
			inConn.commit();
		} catch (Exception e) {
			this.logger.error("删除同步表数据异常:" + e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * @param destFields
	 * @return
	 */
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
