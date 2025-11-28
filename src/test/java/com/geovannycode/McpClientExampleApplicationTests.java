package com.geovannycode;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.ai.mcp.client.enabled=false",
        "spring.ai.mcp.client.annotation-scanner.enabled=false",
        "spring.ai.mcp.client.initialized=false"
})
class McpClientExampleApplicationTests {

	@Test
	void contextLoads() {
	}
}
