package be.mrtus.container.simple;

import be.mrtus.container.Container;
import be.mrtus.container.ContainerAware;
import be.mrtus.container.ServiceNotFound;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleConfigurableContainer implements Container, ConfigurableContainer {

	private final Map<Class, Supplier> configuredServices = new HashMap<>();
	private Container delegate;
	private final Map<Class, Object> serviceInstances = new HashMap<>();

	public SimpleConfigurableContainer() {
		this(new NullContainer());
	}

	public SimpleConfigurableContainer(
			Container delegate
	) {
		this.serviceInstances.put(Container.class, this);

		this.delegate = delegate;

		if(this.delegate instanceof ContainerAware) {
			ContainerAware containerAwareDelegate = (ContainerAware)this.delegate;

			containerAwareDelegate.setContainer(this);
		}
	}

	@Override
	public void addServiceProvider(Class<? extends ServiceProvider> serviceProviderClass) {
		ServiceProvider serviceProvider = this.createServiceProviderInstance(serviceProviderClass);

		this.addServiceProvider(serviceProvider);
	}

	@Override
	public <T> T get(Class<T> serviceClass) {
		T serviceInstance = (T)this.serviceInstances.get(serviceClass);
		if(serviceInstance != null) {
			return serviceInstance;
		}

		Supplier<T> supplier = this.configuredServices.getOrDefault(
				serviceClass,
				() -> this.delegate.get(serviceClass)
		);

		serviceInstance = supplier.get();
		if(serviceInstance == null) {
			throw new ServiceNotFound("Instance for class '" + serviceClass.toString() + "' could not be created");
		}

		this.serviceInstances.put(serviceClass, serviceInstance);

		return serviceInstance;
	}

	@Override
	public <T> void share(Class<T> serviceClass, Supplier<T> supplier) {
		this.configuredServices.put(serviceClass, supplier);
	}

	private ServiceProvider createServiceProviderInstance(Class<? extends ServiceProvider> serviceProviderClass) {
		try {
			Constructor<? extends ServiceProvider> serviceProviderConstructor = serviceProviderClass.getConstructor();

			return serviceProviderConstructor.newInstance();
		} catch(NoSuchMethodException
				| InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException exception) {
			throw new CouldNotInstantiateServiceProvider(exception);
		}
	}
}
