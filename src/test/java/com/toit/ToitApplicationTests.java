package com.toit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import io.awspring.cloud.s3.S3Template;

@SpringBootTest(properties = {
		"spring.autoconfigure.exclude=io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration"
})
class ToitApplicationTests {

	@MockBean
	private S3Template s3Template;

	@Test
	void contextLoads() {
	}

}
