package com.android.carair.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeanWrapper
{

	public BeanWrapper(Object bean)
	{
		this.bean = bean;
		this.clazz = bean.getClass();
	}

	public BeanWrapper(String beanName)
	{
		Object bean = null;
		try
		{
			bean = Class.forName(beanName).newInstance();
		} catch (ClassNotFoundException e)
		{

		} catch (IllegalAccessException e)
		{

		} catch (InstantiationException e)
		{

		}
		this.bean = bean;
		this.clazz = bean.getClass();
	}

	private final Object bean;

	private final Class clazz;

	public Object call(String methodName)
	{
		Object ret = null;
		try
		{
			Method method = clazz.getMethod(methodName, null);
			ret = method.invoke(bean, null);
		} catch (SecurityException e)
		{
		} catch (NoSuchMethodException e)
		{
		} catch (IllegalArgumentException e)
		{
		} catch (IllegalAccessException e)
		{
		} catch (InvocationTargetException e)
		{
		}

		return ret;
	}

	public Object call(String methodName, Object... args)
	{
		Object ret = null;
		Class[] types = new Class[args.length];
		for (int i = 0; i < types.length; i++)
		{
			types[i] = args[i].getClass();
		}
		try
		{
			Method method = clazz.getMethod(methodName, types);
			ret = method.invoke(bean, args);
		} catch (SecurityException e)
		{
		} catch (NoSuchMethodException e)
		{

		} catch (IllegalArgumentException e)
		{

		} catch (IllegalAccessException e)
		{

		} catch (InvocationTargetException e)
		{

		}

		return ret;
	}

	public static Class findClass(String className)
	{
		Class cls = null;
		try
		{
			cls = Class.forName(className);
		} catch (ClassNotFoundException e)
		{
			
		}
		return cls;
	}

	public static Object findObject(String beanName)
	{
		Object bean = null;
		try
		{
			bean = Class.forName(beanName).newInstance();
		} catch (ClassNotFoundException e)
		{

		} catch (IllegalAccessException e)
		{

		} catch (InstantiationException e)
		{

		}
		return bean;
	}

	public static Object exec(Class clazz, String methodName)
	{
		Object ret = null;
		try
		{
			Method method = clazz.getMethod(methodName, null);
			ret = method.invoke(null);
		} catch (SecurityException e)
		{

		} catch (NoSuchMethodException e)
		{

		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block

		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block

		} catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block

		}
		return ret;
	}

	public static Object exec(Class clazz, String methodName, Object... args)
	{
		Object ret = null;
		Class[] types = new Class[args.length];
		for (int i = 0; i < types.length; i++)
		{
			types[i] = args[i].getClass();
		}
		try
		{
			Method method = clazz.getMethod(methodName, types);
			ret = method.invoke(args);
		} catch (SecurityException e)
		{

		} catch (NoSuchMethodException e)
		{

		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block

		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block

		} catch (InvocationTargetException e)
		{
			// TODO Auto-generated catch block

		}
		return ret;
	}

	public static Object fetchStaticProperty(Class cls, String propertyName)
	{
		Object ret = null;
		try
		{
			Field field = cls.getField(propertyName);
			ret = field.get(null);
		} catch (SecurityException e)
		{

		} catch (NoSuchFieldException e)
		{

		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block

		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block

		}
		return ret;
	}

	public Object getProperty(String propertyName)
	{
		Object ret = null;
		try
		{
			Field field = null;
			Class cls = clazz;
			while (null == field && null != cls && !cls.isInterface())
			{
				field = findField(cls, propertyName);
				cls = cls.getSuperclass();
			}
			if (null == field)
			{
				return null;
			}
			field.setAccessible(true);
			ret = field.get(this.bean);
			field.setAccessible(false);
		} catch (SecurityException e)
		{

		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block

		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block

		}
		return ret;
	}

	private Field findField(Class<?> cls, String propertyName)
	{
		Field field = null;
		try
		{
			field = cls.getDeclaredField(propertyName);

		} catch (NoSuchFieldException e)
		{

		}
		return field;
	}

	public boolean setProperty(String propertyName, Object value)
	{
		boolean success = false;
		try
		{
			Field field = null;
			Class<?> cls = clazz;
			while (null == field && null != cls && !cls.isInterface())
			{
				field = findField(cls, propertyName);
				cls = cls.getSuperclass();
			}
			if (null == field)
			{
				return false;
			}
			field.setAccessible(true);
			field.set(bean, value);
			field.setAccessible(false);
			success = true;
		} catch (SecurityException e)
		{

		} catch (IllegalArgumentException e)
		{
			// TODO Auto-generated catch block

		} catch (IllegalAccessException e)
		{
			// TODO Auto-generated catch block

		}
		return success;
	}

}
