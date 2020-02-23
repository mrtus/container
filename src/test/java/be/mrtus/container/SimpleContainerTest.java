package be.mrtus.container;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SimpleContainerTest {

	private SimpleContainer container;

	@BeforeEach
	public void before() {
		this.container = new SimpleContainer();
	}

	@Test
	public void itShouldRegisterAndGet() {
		var object = new Object();

		this.container.register(Object.class, object);

		var actual = this.container.get(Object.class);

		Assertions.assertEquals(object, actual);
	}

	@Test
	public void itShouldThrowWhenFetchingNullClass() {
		Assertions.assertThrows(
				NullPointerException.class,
				() -> {
					this.container.get(null);
				}
		);
	}

	@Test
	public void itShouldThrowWhenRegisteringWithNullClass() {
		Assertions.assertThrows(
				NullPointerException.class,
				() -> {
					this.container.register(null, new Object());
				}
		);
	}

	@Test
	public void itShouldThrowWhenRegisteringWithNullInstance() {
		Assertions.assertThrows(
				NullPointerException.class,
				() -> {
					this.container.register(Object.class, null);
				}
		);
	}
}
