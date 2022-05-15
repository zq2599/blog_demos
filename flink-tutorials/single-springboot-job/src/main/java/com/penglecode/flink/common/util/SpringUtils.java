package com.penglecode.flink.common.util;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * Spring Bean工具类
 *
 * @author pengpeng
 * @version 1.0
 * @since 2021/5/15 14:02
 */
@SuppressWarnings({"unchecked"})
public class SpringUtils {

	private static volatile ApplicationContext applicationContext;
	
	private static volatile Environment environment;

	private SpringUtils() {}

	/**
	 * 获取Spring环境变量
	 * @param name
	 * @param constType
	 * @return
	 */
	public static <T> T getEnvProperty(String name, Class<T> constType) {
		return getEnvironment().getProperty(name, constType);
	}
	
	/**
	 * 获取Spring环境变量
	 * @param name
	 * @param constType
	 * @param defaultValue
	 * @return
	 */
	public static <T> T getEnvProperty(String name, Class<T> constType, T defaultValue) {
		return getEnvironment().getProperty(name, constType, defaultValue);
	}
	
	public static <T> T getBean(Class<T> requiredType) {
		return getApplicationContext().getBean(requiredType);
	}
	
	public static <T> T getBeanQuietly(Class<T> requiredType) {
		try {
			return getBean(requiredType);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}
	
	public static <T> T getBean(String name, Class<T> requiredType) {
		return getApplicationContext().getBean(name, requiredType);
	}
	
	public static <T> T getBeanQuietly(String name, Class<T> requiredType) {
		try {
			return getBean(name, requiredType);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}
	
	public static <T> T getBean(String name) {
		return (T) getApplicationContext().getBean(name);
	}
	
	public static <T> T getBeanQuietly(String name) {
		try {
			return getBean(name);
		} catch (NoSuchBeanDefinitionException e) {
			return null;
		}
	}
	
	public static <T> Map<String,T> getBeansOfType(Class<T> type) {
		return getApplicationContext().getBeansOfType(type);
	}
	
	public static Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
		return getApplicationContext().getBeansWithAnnotation(annotationType);
	}
	
	public static ApplicationContext getApplicationContext() {
		Assert.state(applicationContext != null, "Global ROOT ApplicationContext has not been initialized yet!");
		return applicationContext;
	}
	
	public static void setApplicationContext(ApplicationContext applicationContext){
		SpringUtils.applicationContext = applicationContext;
	}
	
	public static Environment getEnvironment() {
		return environment;
	}

	public static void setEnvironment(Environment environment) {
		SpringUtils.environment = environment;
	}

	public static ApplicationContext getRootApplicationContext(ApplicationContext applicationContext) {
		if(applicationContext != null) {
			return applicationContext.getParent() == null ? applicationContext : getRootApplicationContext(applicationContext.getParent());
		}
		return null;
	}

	/**
	 * 将Environment中指定bindName开头的配置装配到指定的bean类型上
	 *
	 * @param beanClass		- 需要动态注册的bean的类型
	 * @param bindName		- 相当于@ConfigurationProperties注解的prefix字段
	 * @param <T>
	 * @return
	 */
	public static <T> BeanDefinition createBindableBeanDefinition(Class<T> beanClass, String bindName) {
		Assert.notNull(beanClass, "Parameter 'beanClass' must be required!");
		Assert.hasText(bindName, "Parameter 'bindName' must be required!");
		T bindObject = Binder.get(getEnvironment()).bind(bindName, beanClass).orElseThrow(IllegalStateException::new);
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass, () -> bindObject);
		return beanDefinitionBuilder.getBeanDefinition();
	}

	/**
	 * 将Environment中指定bindName开头的配置装配到指定的bean实例上
	 *
	 *
	 * @param beanInstance	- 需要动态注册的bean的给定示例
	 * @param beanClass		- 需要动态注册的bean的类型
	 * @param bindName		- 相当于@ConfigurationProperties注解的prefix字段
	 * @param <T>
	 * @return
	 */
	public static <T> BeanDefinition createBindableBeanDefinition(T beanInstance, Class<T> beanClass, String bindName) {
		Assert.notNull(beanInstance, "Parameter 'beanInstance' must be required!");
		Assert.hasText(bindName, "Parameter 'bindName' must be required!");
		T bindObject = Binder.get(getEnvironment()).bind(bindName, Bindable.ofInstance(beanInstance)).orElse(beanInstance);
		BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClass != null ? beanClass : (Class<T>)beanInstance.getClass(), () -> bindObject);
		return beanDefinitionBuilder.getBeanDefinition();
	}

	/**
	 * 注册bean
	 *
	 * @param beanName
	 * @param beanDefinition
	 */
	public static void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
		Assert.hasText(beanName, "Parameter 'beanName' must be required!");
		Assert.notNull(beanDefinition, "Parameter 'beanDefinition' must be required!");
		GenericApplicationContext genericApplicationContext = (GenericApplicationContext) getApplicationContext();
		genericApplicationContext.registerBeanDefinition(beanName, beanDefinition);
	}

}
