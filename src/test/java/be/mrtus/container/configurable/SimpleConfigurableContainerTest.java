package be.mrtus.container.configurable;

import be.mrtus.container.Container;
import be.mrtus.container.ContainerRegistry;
import java.util.function.Supplier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SimpleConfigurableContainerTest {

	private SimpleConfigurableContainer container;
	private Container delegate;
	private ContainerRegistry registry;

	@BeforeEach
	public void before() {
		this.registry = Mockito.mock(ContainerRegistry.class);
		this.delegate = Mockito.mock(Container.class);

		this.container = new SimpleConfigurableContainer(
			this.registry,
			this.delegate
		);
	}

	@Test
	public void itShouldConfigureServiceProvider() {
		var serviceProvider = Mockito.mock(ServiceProvider.class);

		this.container.addServiceProvider(serviceProvider);

		Mockito.verify(serviceProvider)
			.configure(this.container);
	}

	@Test
	public void itShouldFetchRequestedServiceAndRegisterWithRegistry() {
		var object = new Object();

		Mockito.when(this.registry.get(Object.class))
			.thenReturn(null);

		this.container.share(
			Object.class,
			() -> object
		);

		var actual = this.container.get(Object.class);

		Mockito.verify(this.registry)
			.register(Object.class, object);

		Assertions.assertEquals(object, actual);
	}

	@Test
	public void itShouldFetchRequestedServiceAndRegisterWithRegistryAfterDelegateWasUsed() {
		var object = new Object();

		Mockito.when(this.registry.get(Object.class))
			.thenReturn(null);

		Mockito.when(this.delegate.get(Object.class))
			.thenReturn(object);

		var actual = this.container.get(Object.class);

		Mockito.verify(this.registry)
			.register(Object.class, object);

		Assertions.assertEquals(object, actual);
	}

	@Test
	public void itShouldFetchRequestedServiceFromRegistry() {
		var object = new Object();

		Mockito.when(this.registry.get(Object.class))
			.thenReturn(object);

		var actual = this.container.get(Object.class);

		Mockito.verify(this.registry, Mockito.never())
			.register(Mockito.any(), Mockito.any());

		Assertions.assertEquals(object, actual);
	}

	@Test
	public void itShouldRegisterWithRegistry() {
	}

	@Test
	public void itShouldThrowWhenHandleClassIsNull() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> {
				this.container.handle(null, instance -> {
				}
				);
			}
		);
	}

	@Test
	public void itShouldThrowWhenHandleConsumerIsNull() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> {
				this.container.handle(Object.class, null);
			}
		);
	}

	@Test
	public void itShouldThrowWhenRequestedServiceClassIsNullWhenFetching() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> {
				this.container.get(null);
			}
		);
	}

	@Test
	public void itShouldThrowWhenServiceProviderAsAClassIsNull() {
		Class<ServiceProvider> serviceProviderClass = null;

		Assertions.assertThrows(
			NullPointerException.class,
			() -> {
				this.container.addServiceProvider(serviceProviderClass);
			}
		);
	}

	@Test
	public void itShouldThrowWhenServiceProviderAsAnInstanceIsNull() {
		ServiceProvider serviceProvider = null;

		Assertions.assertThrows(
			NullPointerException.class,
			() -> {
				this.container.addServiceProvider(serviceProvider);
			}
		);
	}

	@Test
	public void itShouldThrowWhenShareAsAClassClassIsNull() {
		Assertions.assertThrows(
			NullPointerException.class,
			() -> {
				this.container.share(null, Object.class);
			}
		);
	}

	@Test
	public void itShouldThrowWhenShareAsASupplierIsNull() {
		Supplier supplier = null;

		Assertions.assertThrows(
			NullPointerException.class,
			() -> {
				this.container.share(null, supplier);
			}
		);
	}

	@Test
	public void itShouldUseConfiguredHandlersService() {
		var object = new Object();

		container.handle(
			Object.class,
			instance -> {
				Assertions.assertEquals(new Object(), instance);
			}
		);

		Mockito.when(this.registry.get(Object.class))
			.thenReturn(null);

		Mockito.when(this.delegate.get(Object.class))
			.thenReturn(object);

		this.container.get(Object.class);
	}
}
