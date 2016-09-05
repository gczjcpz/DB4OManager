package com.toone.ctrl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.db4o.reflect.ReflectClass;
import com.db4o.reflect.ReflectField;
import com.db4o.reflect.generic.GenericClass;
import com.db4o.reflect.generic.GenericObject;

/**
 * 公用方法辅助类
 * @author laiwj
 *
 */
public class Util {

	public static String upper1st(String name) {
		if (name == null)
			return "";
		int len = name.length();
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; ++i) {
			if (i == 0) {
				sb.append(Character.toUpperCase(name.charAt(i)));
			} else {
				sb.append(name.charAt(i));
			}
		}
		return sb.toString();
	}

	public static ReflectField getField(ReflectClass clazz, String fieldName) {
		ReflectField field = clazz.getDeclaredField(fieldName);
		if (field == null && clazz.getSuperclass() != null) {
			return getField(clazz.getSuperclass(), fieldName);
		} else {
			return field;
		}
	}

	public static ReflectField[] getAllFields(ReflectClass clazz) {
		List<ReflectField> fields = new ArrayList<>();
		if (clazz.getSuperclass() != null) {
			ReflectField[] parentField = getAllFields(clazz.getSuperclass());
			for (ReflectField f : parentField) {
				fields.add(f);
			}
		}
		for (ReflectField f : clazz.getDeclaredFields()) {
			if (f.isStatic()) {
				continue;
			}
			if (f.isTransient()) {
				continue;
			}
			fields.add(f);
		}
		ReflectField[] rtn = new ReflectField[fields.size()];
		fields.toArray(rtn);
		return rtn;
	}

	public static Field[] getAllFields(Class<?> clazz) {
		List<Field> fields = new ArrayList<>();
		if (clazz.getSuperclass() != null) {
			Field[] parentField = getAllFields(clazz.getSuperclass());
			for (Field f : parentField) {
				fields.add(f);
			}
		}
		for (Field f : clazz.getDeclaredFields()) {
			if ((f.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
				continue;
			if ((f.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT)
				continue;
			fields.add(f);
		}
		Field[] rtn = new Field[fields.size()];
		fields.toArray(rtn);
		return rtn;
	}

	public static boolean isPrimitiveType(ReflectClass fieldType) {
		return "java.lang.String".equals(fieldType.getName()) || "java.lang.Double".equals(fieldType.getName())
				|| "java.lang.Int".equals(fieldType.getName()) || "java.lang.Float".equals(fieldType.getName())
				|| "java.lang.Boolean".equals(fieldType.getName()) || "java.lang.Char".equals(fieldType.getName())
				|| "double".equals(fieldType.getName()) || "int".equals(fieldType.getName())
				|| "float".equals(fieldType.getName()) || "boolean".equals(fieldType.getName())
				|| "long".equals(fieldType.getName()) || "char".equals(fieldType.getName())
				|| "java.lang.Long".equals(fieldType.getName());
	}

	/**
	 * 判断指定对象是否基本类型
	 * @param object
	 * @return
	 */
	public static boolean isPrimitiveType(Object object) {
		Class<? extends Object> objClass = object.getClass();
		return "java.lang.String".equals(objClass.getName()) || "java.lang.Double".equals(objClass.getName())
				|| "java.lang.Int".equals(objClass.getName()) || "java.lang.Float".equals(objClass.getName())
				|| "java.lang.Boolean".equals(objClass.getName()) || "java.lang.Char".equals(objClass.getName())
				|| "double".equals(objClass.getName()) || "int".equals(objClass.getName())
				|| "float".equals(objClass.getName()) || "boolean".equals(objClass.getName())
				|| "long".equals(objClass.getName()) || "char".equals(objClass.getName())
				|| "java.lang.Long".equals(objClass.getName());
	}

	public static Class<?> tran2Class(ReflectClass clazz) throws ClassNotFoundException {
		return Class.forName(clazz.getName());
	}

	public static GenericClass getActualType(Object value) {
		if (value instanceof Iterable) {
			Iterator<?> iterator = ((Iterable<?>) value).iterator();
			if (iterator.hasNext()) {
				Object next = iterator.next();
				if (next instanceof GenericObject) {
					return ((GenericObject) next).getGenericClass();
				} else {
					return null;
				}
			} else {
				return null;
			}
		} else if (value instanceof Map) {
			return null;
		} else {
			if (value instanceof GenericObject) {
				return ((GenericObject) value).getGenericClass();
			} else {
				return null;
			}
		}
	}

	public static boolean isCollection(Object value) {
		return value instanceof Iterable;
	}

	public static boolean isEmptyCollection(Object value) {
		if (value instanceof Iterable) {
			Iterator<?> iterator = ((Iterable<?>) value).iterator();
			return !iterator.hasNext();
		} else {
			return false;
		}
	}

	@Deprecated
	/**
	 * 反射获取指定对象中指定字段的值
	 * @param obj 指定对象
	 * @param f 指定字段
	 * @return
	 */
	public static Object getFieldValue(Object obj, Field f) {
		try {
			Method method = null;
			if (isBoolean(f.getType())) {
				try {
					method = obj.getClass().getMethod("is" + upper1st(f.getName()), new Class<?>[] {});
				} catch (NoSuchMethodException e) {
					try {
						method = obj.getClass().getMethod("get" + upper1st(f.getName()), new Class<?>[] {});
					} catch (NoSuchMethodException e1) {
						try {
							method = obj.getClass().getMethod(f.getName(), new Class<?>[] {});
						} catch (NoSuchMethodException e2) {
							e2.printStackTrace();
							method = null;
						}
					}
					return method != null ? method.invoke(obj, new Object[] {}) : null;
				}
			} else {
				try {
					method = obj.getClass().getMethod("get" + upper1st(f.getName()), new Class<?>[] {});
				} catch (NoSuchMethodException e) {
					try {
						method = obj.getClass().getMethod("get" + f.getName(), new Class<?>[] {});
					} catch (NoSuchMethodException e1) {
						try {
							method = obj.getClass().getMethod("get" + f.getName().toUpperCase(), new Class<?>[] {});
						} catch (NoSuchMethodException e2) {
							try {
								method = obj.getClass().getMethod("get" + f.getName().toLowerCase(), new Class<?>[] {});
							} catch (NoSuchMethodException e3) {
								return null;
							}
						}
					}
				}
				return method.invoke(obj, new Object[] {});
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static boolean isBoolean(Class<?> type) {
		return "boolean".equals(type.getName()) || "java.lang.Boolean".equals(type.getName());
	}

	public static long getSize(Iterable<?> value) {
		if (value instanceof Collection) {
			return ((Collection<?>) value).size();
		} else {
			long count = 0;
			Iterator<?> iterator = value.iterator();
			while (iterator.hasNext()) {
				iterator.next();
				++count;
			}
			return count;
		}
	}
}
