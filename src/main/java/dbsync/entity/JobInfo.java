package dbsync.entity;

import java.io.Serializable;

/**
 * @author zhaonan
 * @date 2020/9/1 15:30
 * @description 任务信息
 * @version 1.0.0
 */
public class JobInfo implements Serializable {
	private static final long serialVersionUID = -1907092113028096170L;

	// 任务名称
	private String name;
	// 任务表达式
	private String cron;
	// 源数据源sql
	private String srcSql;
	// 源表数据字段
	private String srcTableFields;
	// 目标数据表
	private String destTable;
	// 目标表数据字段
	private String destTableFields;
	// 目标表主键
	private String destTableKey;
	// 目标表可更新的字段
	private String destTableUpdate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getSrcSql() {
		return srcSql;
	}

	public void setSrcSql(String srcSql) {
		this.srcSql = srcSql;
	}

	public String getDestTable() {
		return destTable;
	}

	public void setDestTable(String destTable) {
		this.destTable = destTable;
	}

	public String getDestTableFields() {
		return destTableFields;
	}

	public void setDestTableFields(String destTableFields) {
		this.destTableFields = destTableFields;
	}

	public String getDestTableKey() {
		return destTableKey;
	}

	public void setDestTableKey(String destTableKey) {
		this.destTableKey = destTableKey;
	}

	public String getDestTableUpdate() {
		return destTableUpdate;
	}

	public void setDestTableUpdate(String destTableUpdate) {
		this.destTableUpdate = destTableUpdate;
	}

	/**
	 * @return the srcTableFields
	 */
	public String getSrcTableFields() {
		return srcTableFields;
	}

	/**
	 * @param srcTableFields the srcTableFields to set
	 */
	public void setSrcTableFields(String srcTableFields) {
		this.srcTableFields = srcTableFields;
	}
}
