/**
 * 
 */
package dbsync.build;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import dbsync.constants.Constants;
import dbsync.entity.DBInfo;
import dbsync.entity.JobInfo;
import dbsync.task.JobTask;

/**
 * @author liuyazhuang
 * @date 2018/9/11 10:08
 * @description 同步数据库数据的Builder对象
 * @version 1.0.0
 */
public class DBSyncBuilder {

	private DBInfo srcDb;
	private DBInfo destDb;
	private List<JobInfo> jobList;
	private String code;
	private static Logger logger = Logger.getLogger(DBSyncBuilder.class);

	private DBSyncBuilder() {
	}

	/**
	 * 创建DBSyncBuilder对象
	 * 
	 * @return DBSyncBuilder对象
	 */
	public static DBSyncBuilder builder() {
		return new DBSyncBuilder();
	}

	/**
	 * 初始化数据库信息并解析jobs.xml填充数据
	 * 
	 * @return DBSyncBuilder对象
	 */
	public DBSyncBuilder init() {
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
	 * 启动定时任务，同步数据库的数据
	 */
	public void start() {
		for (int index = 0; index < jobList.size(); index++) {
			JobInfo jobInfo = jobList.get(index);
			String logTitle = "[" + code + "]" + jobInfo.getName() + " ";
			JobTask job = new JobTask();
			job.getJobDataMap().put("srcDb", srcDb);
			job.getJobDataMap().put("destDb", destDb);
			job.getJobDataMap().put("jobInfo", jobInfo);
			job.getJobDataMap().put("logTitle", logTitle);
			logger.info(jobInfo.getCron());
			while(true) {
				try {
					Thread.sleep(1000);
					job.run();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}
			
		}
	}
}
