package com.atguigu.gmall.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {
	@Autowired
	JestClient jestClient;
	@Test
	public void contextLoads()  {
		String query="{\n" +
				"  \"query\": {\n" +
				"    \"match\": {\n" +
				"      \"name\": \"草莓\"\n" +
				"    }\n" +
				"  }\n" +
				"}";
		Search search = new Search.Builder(query).addIndex("fruits_index").addType("fruit").build();
		SearchResult result = null;
		try {
			result = jestClient.execute(search);
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<SearchResult.Hit<HashMap, Void>> hits = result.getHits(HashMap.class);
		for (SearchResult.Hit<HashMap, Void> hit : hits) {
			HashMap source = hit.source;
			System.out.println("source = " + source);
		}
	}

}
