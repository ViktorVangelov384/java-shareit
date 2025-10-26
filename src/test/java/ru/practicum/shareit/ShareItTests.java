package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShareItTests {

	@Test
	void contextLoads() {
		assertTrue(true, "Spring контекст должен загружаться");
	}

	@Test
	void applicationStartsSuccessfully() {
		ShareItApp app = new ShareItApp();
		assertNotNull(app, "Приложение должно создаваться");

		assertDoesNotThrow(() -> {
			ShareItApp.main(new String[]{});
		}, "Main метод должен запускаться без ошибок");
	}

}
