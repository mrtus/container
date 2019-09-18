package be.mrtus.container.reflection;

import be.mrtus.container.Container;
import be.mrtus.container.ContainerAware;
import be.mrtus.container.ServiceNotFound;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReflectionContainer implements Container, ContainerAware {

	private Container container;

	@Override
	public <T> T get(Class<T> serviceClass) {
		try {
			return this.createInstance(serviceClass);
		} catch(FittingConstructorNotFound exception) {
			throw new ServiceNotFound("Instance for class '" + serviceClass.toString() + "' could not be created");
		}
	}

	private Container getContainer() {
		if(this.container == null) {
			this.container = this;
		}

		return this.container;
	}

	@Override
	public void setContainer(Container container) {
		this.container = container;
	}

	private <T> T createInstance(Class<T> serviceClass) {
		Constructor<T> constructor = this.findMostFittingConstructor(serviceClass);

		return this.createInstanceFromConstructor(constructor);
	}

	private <T> T createInstanceFromConstructor(Constructor<T> constructor) {
		List<Object> servicesList = Arrays.asList(constructor.getParameterTypes())
				.stream()
				.map(param -> this.getContainer().get(param))
				.collect(Collectors.toList());

		try {
			return constructor.newInstance(servicesList.toArray());
		} catch(InstantiationException
				| IllegalAccessException
				| IllegalArgumentException
				| InvocationTargetException exception) {
			throw new RuntimeException(exception);
		}
	}

	private Constructor findMostFittingConstructor(Class serviceClass) {
		Optional<Constructor> constructor = Arrays.asList(serviceClass.getConstructors())
				.stream()
				.filter(constructorMethod -> {
					List<Class> constructorParameters = Arrays.asList(constructorMethod.getParameterTypes());

					try {
						constructorParameters.stream()
								.forEach(param -> this.getContainer().get(param));
					} catch(ServiceNotFound exception) {
						return false;
					}

					return true;
				})
				.sorted(new Comparator<Constructor>() {
					@Override
					public int compare(Constructor a, Constructor b) {
						return Integer.compare(a.getParameterCount(), b.getParameterCount());
					}
				}.reversed())
				.findFirst();

		return constructor.orElseThrow(() -> new FittingConstructorNotFound());
	}
}
