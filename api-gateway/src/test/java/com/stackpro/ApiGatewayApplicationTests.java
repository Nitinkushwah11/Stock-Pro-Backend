package com.stackpro;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.stockpro.ApiGatewayApplication;

@SpringBootTest(
		classes = ApiGatewayApplication.class,
		properties = "jwt.secret=test-secret-for-gateway-context")
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}
