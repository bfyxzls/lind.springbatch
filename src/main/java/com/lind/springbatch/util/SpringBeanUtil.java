package com.lind.springbatch.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

/**
 * .
 *
 * @Auther: wangjunbo
 * @Date: 18/12/13 11:40
 * @Description:
 */
public class SpringBeanUtil {
  private static ApplicationContext applicationContext;
  static final private Object lock = new Object();

  /**
   * 设置上下文.
   */
  public static void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    synchronized (lock) {
      if (SpringBeanUtil.applicationContext == null) {
        SpringBeanUtil.applicationContext = applicationContext;
      }
    }
  }

  /**
   * 获取applicationContext.
   */
  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  /**
   * 通过name获取 Bean.
   */
  public static Object getBean(String name) {
    return getApplicationContext().getBean(name);
  }

  /**
   * 通过class获取Bean.
   */
  public static <T> T getBean(Class<T> clazz) {
    return getApplicationContext().getBean(clazz);
  }

  /**
   * 通过name,以及Clazz返回指定的Bean.
   */
  public static <T> T getBean(String name, Class<T> clazz) {
    return getApplicationContext().getBean(name, clazz);
  }
}
