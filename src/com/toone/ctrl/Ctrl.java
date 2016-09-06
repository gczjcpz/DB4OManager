package com.toone.ctrl;import java.util.Collections;import java.util.List;import java.util.stream.Collectors;import com.db4o.reflect.ReflectClass;import com.toone.db.DataSource;public class Ctrl {	private DataSource ds;	public Ctrl(String DBURL) {		ds = new DataSource();		ds.openDB(DBURL);	}	/**	 * 获取数据库中所有用户定义的类	 * @return	 */	public List<ReflectClass> getAllUserClass() {		List<ReflectClass> allClass = ds.getAllUserClass();		return allClass.stream().filter(e -> !(e.getName().startsWith("java")						|| e.getName().startsWith("com.db4o") || e.getName().startsWith("Db4o")))				.collect(Collectors.toList());	}	/**	 * 获取指定类在数据库中的全部数据	 * @param className 类名	 * @return	 */	public <T> List<T> getAllData(String className) {		try {			return ds.getAll(className);		} catch (Exception e) {			return Collections.emptyList();		}	}	/**	 * 指定对象在数据库中的唯一标识	 * @param object	 * @return	 */	public long getOID(Object object) {		return ds.getOID(object);	}		/**	 * 以指定的激活深度激活指定对象。11	 * <br>如果对象是个单对象（不是一个集合），未激活时，所有属性是空的，激活1层则读取所有基本类型的值，不读取引用类型（不论是单对象还是集合）	 * <br>如果对象是一个集合，未激活时，集合是空的，激活1层则把集合里所有项激活1层	 * @param object 指定对象	 * @param activeDepth 激活深度	 */	public void activate(Object object, int activeDepth){		if(!isActive(object)){			ds.getDB().activate(object, activeDepth);		}else{			System.out.println(object + "is actived");		}	}		/**	 * 解除对象激活	 * @param object	 * @param deactivateDepth	 */	public void deactivate(Object object, int deactivateDepth){		ds.getDB().deactivate(object, deactivateDepth);	}	/**	 * 判断一个对象是否已经被激活	 * @param object	 * @return	 */	public boolean isActive(Object object){		return ds.getDB().ext().isActive(object);	}	/**	 * 关闭数据库连接	 */	public void closeDB() {		ds.getDB().close();	}}	