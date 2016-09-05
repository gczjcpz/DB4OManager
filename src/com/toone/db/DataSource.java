package com.toone.db;
import java.util.List;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.ConfigScope;
import com.db4o.config.Configuration;
import com.db4o.objectmanager.api.helpers.ReflectHelper2;
import com.db4o.query.Query;
import com.db4o.reflect.ReflectClass;
import com.db4o.reflect.generic.GenericReflector;

public class DataSource {

    private ObjectContainer db;

    /**
     * 
     * @param dbUrl 数据库文件路径
     */
    public void openDB(String dbUrl){
		Configuration conf = Db4o.newConfiguration();//数据库的全局化配置
        renderDB4OConfig(conf);
        db = Db4o.openFile(conf, dbUrl);
    }

    @SuppressWarnings("unchecked")
	public List<ReflectClass> getAllUserClass(){
    	return ReflectHelper2.getUserStoredClasses(db);
    }

    public ObjectContainer getDB(){
    	return db;
    }
    
    public <T> List<T> getAll(String name){
        Query query = db.query();
		query.constrain(db.ext().reflector().forName(name));
		ObjectSet resultSet = query.execute();
        if(resultSet.hasNext()){
            return resultSet;
        }
        return java.util.Collections.emptyList();
    }

    public long getOID(Object obj){
        return db.ext().getID(obj);
    }
    
    public GenericReflector getReflector(){
    	return db.ext().reflector();
    }

	public void save(Object object) {
		db.set(object);
	}
	
	public void commit(){
		db.commit();
	}
	
	public static void renderDB4OConfig(Configuration conf) {
		// if (!isActiceAll) {
		// conf.add(new TransparentActivationSupport());//
		// 透明的激活，自动级联，配置后集合数据无法读取
		// }
		conf.activationDepth(1);
		conf.blockSize(8);// 设置存储块的大小：建议设置成8（内部指针大小）
		conf.lockDatabaseFile(true); // 是否锁定数据库
		// 指定一个打印流（比如文件流）。默认是System.out
		// 允许自动升级旧版本的数据库
		conf.allowVersionUpdates(true);
		/**
		 * 不自动关闭数据库会导致数据库文件不能释放部分空间，但不会影响文件使用。<br>
		 * 可结合Defragment来回收空间。
		 */
		conf.automaticShutDown(false);//
		/**
		 * 关闭所有的回调方法，因为db4o在启动时会扫描所有方法，以检查回调
		 */
		conf.callbacks(false);
		/**
		 * 此设置可提高性能，但是每一个持久化类必须有一个无参数构造函数<br>
		 * 单独设置一个类的方法：conf.objectClass(Foo.class).callConstructor(true)
		 */
		conf.callConstructors(true);// 是否使用构造函数实例化对象
		/**
		 * 设置是否测试构造函数，默认为true，若所持久化类都有一个无参数构造函，则应设置为false
		 */
		conf.testConstructors(false);
		/**
		 * 当设置为true时，可以保证提交的正确（事务控制），但对性能有很大影响。
		 */
		conf.flushFileBuffers(true);// 设置为false会导致某些系统上文件保存出错
		/**
		 * 影响性能<br>
		 * Integer.MAX_VALUE:启动时丢弃所有空间碎片。影响: 数据文件会快速增长。<br>
		 * 0:默认设置，所有释放空间会被再次使用。影响：增加内存消耗，并且性能下降当维护RAM（内存）中的碎片列表时。<br>
		 * 注：设置为Integer.MAX_VALUE时，需要结合Defragment来回收空间。
		 */
		conf.freespace().discardSmallerThan(Integer.MAX_VALUE);
		/**
		 * 优化本地化查询
		 */
		conf.optimizeNativeQueries(true);

		/**
		 * 配置db4o针对所有对象均不生成uuid
		 */
		conf.generateUUIDs(ConfigScope.DISABLED);
		/**
		 * 设置日志输出级别 Level 0 - no messages<br>
		 * Level 1 - open and close messages<br>
		 * Level 2 - messages for new, update and delete<br>
		 * Level 3 - messages for activate and deactivate<br>
		 */
		conf.messageLevel(0);
		/**
		 * 去除弱引用（weakReferences(false)）：将停止在维护一致性上的开销,但应用必须自己通过调用# purge来从引用树上移除对象<br>
		 * #purge（净化对象）：将从引用缓存中移除一个对象，次方法可将引用树保存在最小。<br>
		 * 调用#purge(object)后，对象对于ObjectContainer变为unknown状态，因此此特征是有效的为批量插入
		 */
		conf.weakReferences(false);// 默认=true
		/**
		 * 设置弱引用收集的周期。设为0时为去除弱引用维护。<br>
		 * 默认＝1000milliseconds(1s)
		 */
		conf.weakReferenceCollectionInterval(0);//
		/**
		 * 设置BTree在索引上的大小<br>
		 * 默认值100,此参数配置的越大读写速度越快,但是内存消耗也越大
		 */
		conf.bTreeNodeSize(100);
		/**
		 * 配置BTree节点的缓存<br>
		 * BTree缓存节点的计算:maxCachedNodes = bTreeNodeSize ^ bTreeCacheHeight
		 */
		conf.bTreeCacheHeight(1);

		/**
		 * 配置db4o是否在启动时检查所有类是否有增加或者删除的字段<br>
		 * 默认true，但是在发布产品时应该改为false,发布产品时应该改为false<br>
		 * FIXME:此方法如何配置，需要进行测试才能确定，其他参考((ExtClient)objectContainer). switchToFile(databaseFile)<br>
		 */
		// conf.detectSchemaChanges(false);
		/**
		 * 其他提升性能的方式：<br>
		 * 1、减少继承的层次和接口的层次，因为db4o需要维护索引<br>
		 * 2、将不需要存储的字段设置为transient<br>
		 * 3、激活层次控制<br>
		 * 4、使用reserveStorageSpace在新建文件时分配一个足够大的空间。<br>
		 * 5、使用conf.objectClass("classname").indexed(false);来关闭指定类的索引。<br>
		 * 6、结合CachedIoAdapter和RandomAccessFileAdapter提升性能：conf(new CachedIoAdapter(delegateAdapter, page_size, page_count));<br>
		 */
		conf.io(new TIOAdapter());

	}
}
