package com.atguigu.gmall.manage;

import com.atguigu.gmall.manage.service.impl.HtmlParseServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ComponentScan("com.atguigu.gmall")
@SpringBootTest
public class GmallManageServiceApplicationTests {
	@Autowired
	HtmlParseServiceImpl htmlParseServiceImpl;
	@Test
	public void contextLoads() {
		htmlParseServiceImpl.parseHtml();
	}

}
