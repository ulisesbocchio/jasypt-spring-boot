package demo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SimpleDemoApplication.class)
public class DemoApplicationTests {

	static {
		System.setProperty("jasypt.encryptor.password", "password");
	}

	@Test
	public void contextLoads() {
	}

}
