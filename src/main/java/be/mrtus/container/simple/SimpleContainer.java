package be.mrtus.container.simple;

import be.mrtus.container.Container;
import be.mrtus.container.ContainerAware;
import be.mrtus.container.ServiceNotFound;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleContainer implements Container, ConfigurableContainer {

	private final Map<Class, Consumer> configuredHandlers = new HashMap<>();
	private final Map<Class, Supplier> configuredServices = new HashMap<>();
	private Container delegate;
	private final Map<Class, Object> serviceInstances = new HashMap<>();

	public SimpleContainer() {
		this(new NullContainer());
	}

	public SimpleContainer(
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

		this.applyHandlers(serviceInstance);

		return serviceInstance;
	}

	@Override
	public <T> void handle(Class<T> serviceClass, Consumer<T> consumer) {
		this.configuredHandlers.put(serviceClass, consumer);
	}

	@Override
	public <T> void share(Class<T> serviceClass, Supplier<T> supplier) {
		this.configuredServices.put(serviceClass, supplier);
	}

	private void applyHandlers(Object instance) {
		var classOfInstance = instance.getClass();

		List<Class> classesList = new ArrayList<>();

		classesList.addAll(Arrays.asList(classOfInstance.getInterfaces()));

		classesList.addAll(this.findSuperClassesOfClass(classOfInstance));

		classesList.forEach(instanceClass -> {
			var handler = this.configuredHandlers.get(instanceClass);

			if(handler == null) {
				return;
			}

			handler.accept(instance);
		});
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

	private List<Class> findSuperClassesOfClass(Class instanceClass) {
		var superClass = instanceClass.getSuperclass();

		if(superClass == null) {
			return Arrays.asList();
		}

		List<Class> list = new ArrayList<>();

		list.add(superClass);
		list.addAll(this.findSuperClassesOfClass(superClass));

		return list;
	}
}
