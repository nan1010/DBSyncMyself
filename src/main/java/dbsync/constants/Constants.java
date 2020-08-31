/**
 * Copyright 2018-2118 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dbsync.constants;

/**
 * @author liuyazhuang
 * @date 2018/9/11 10:15
 * @description 常量类
 * @version 1.0.0
 */
public class Constants {

	/**
	 * 日期分割符
	 */
	public static final String DATE_SPLIT = "-";

	/**
	 * 时间分割符
	 */
	public static final String TIME_SPLIT = ":";

	/**
	 * 配置的字段分隔符
	 */
	public static final String FIELD_SPLIT = ",";

	/**
	 * 源数据库
	 */
	public static final String TYPE_SOURCE = "source";

	/**
	 * 目标数据库
	 */
	public static final String TYPE_DEST = "dest";

	/**
	 * MySQL数据库
	 */
	public static final String TYPE_DB_MYSQL = "mysql";

	/**
	 * 序列化标识的字段
	 */
	public static final String FIELD_SERIALVERSIONUID = "serialVersionUID";

	/**
	 * 配置文件的目录
	 */
	public static final String JOB_CONFIG_FILE = "/mykit-db-transfer/mysql_mysql_jobs.xml";

	/**
	 * 对应xml文件的source节点
	 */
	public static final String NODE_SOURCE = "source";
	/**
	 * 对应xml文件的dest节点
	 */
	public static final String NODE_DEST = "dest";
	/**
	 * 对应xml文件的jobs节点
	 */
	public static final String NODE_JOBS = "jobs";
	/**
	 * 对应xml文件的job节点
	 */
	public static final String NODE_JOB = "job";
	/**
	 * 对应xml文件的code节点
	 */
	public static final String NODE_CODE = "code";
	/**
	 * 源数据库
	 */
	public static final String SRC_DB = "srcDb";
	/**
	 * 目标数据库
	 */
	public static final String DEST_DB = "destDb";
	/**
	 * 任务信息
	 */
	public static final String JOB_INFO = "jobInfo";
	/**
	 * 日志标头
	 */
	public static final String LOG_TITLE = "logTitle";
	/**
	 * job前缀
	 */
	public static final String JOB_PREFIX = "job-";
	/**
	 * trigger前缀
	 */
	public static final String TRIGGER_PREFIX = "trigger-";

}
