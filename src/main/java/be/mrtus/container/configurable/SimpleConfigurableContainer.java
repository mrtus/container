package be.mrtus.container.configurable;

import be.mrtus.container.Container;
import be.mrtus.container.ContainerRegistry;
import be.mrtus.container.ServiceNotFound;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimpleConfigurableContainer implements Container, ConfigurableContainer {

	private final Map<Class, Consumer> configuredHandlers = new HashMap<>();
	private final Map<Class, Supplier> configuredServices = new HashMap<>();
	private Container delegate;
	private ContainerRegistry registry;

	public SimpleConfigurableContainer(
			ContainerRegistry registry
	) {
		this(
				registry,
				new NullContainer()
		);
	}

	public SimpleConfigurableContainer(
			ContainerRegistry registry,
			Container delegate
	) {
		this.registry = registry;
		this.delegate = delegate;
	}

	@Override
	public void addServiceProvider(Class<? extends ServiceProvider> serviceProviderClass) {
		Objects.requireNonNull(serviceProviderClass, "Service provider class cannot be null");

		ServiceProvider serviceProvider = this.createServiceProviderInstance(serviceProviderClass);

		this.addServiceProvider(serviceProvider);
	}

	@Override
	public <T> T get(Class<T> serviceClass) {
		Objects.requireNonNull(serviceClass, "Service class cannot be null");

		T serviceInstance = this.registry.get(serviceClass);
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

		this.registry.register(serviceClass, serviceInstance);

		this.applyHandlers(serviceInstance);

		return serviceInstance;
	}

	@Override
	public <T> void handle(Class<T> serviceClass, Consumer<T> consumer) {
		Objects.requireNonNull(serviceClass, "Service class cannot be null");
		Objects.requireNonNull(consumer, "Service consumer cannot be null");

		this.configuredHandlers.put(serviceClass, consumer);
	}

	@Override
	public <T> void share(Class<T> serviceClass, Supplier<T> supplier) {
		Objects.requireNonNull(serviceClass, "Service class cannot be null");
		Objects.requireNonNull(supplier, "Service supplier cannot be null");

		this.configuredServices.put(serviceClass, supplier);
	}

	private void applyHandlers(Object instance) {
		var classOfInstance = instance.getClass();

		List<Class> classesList = new ArrayList<>();

		classesList.addAll(this.findInterfacesOfClass(classOfInstance));
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

	private Collection<? extends Class> findInterfacesOfClass(Class<?> classOfInstance) {
		var directInterfacesList = Arrays.asList(classOfInstance.getInterfaces());

		List<Class> list = new ArrayList<>();

		list.addAll(directInterfacesList);

		directInterfacesList.forEach(interfaceClass -> {
			var interfaces = this.findInterfacesOfClass(interfaceClass);

			if(interfaces.isEmpty()) {
				return;
			}

			list.addAll(interfaces);
		});

		return list;
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
