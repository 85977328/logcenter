package com.panguso.lc.analysis.format;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	private static ApplicationContext context;

	public static void main(String[] args) throws Exception {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
		Properties prop = context.getBean("configProperties", Properties.class);

		logger.info(prop.toString());
		// int res = ToolRunner.run(new Configuration(), new Logcenter(), args);
		// System.exit(res);
		
	}
}
