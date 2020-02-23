package be.mrtus.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

final public class SimpleContainer implements ContainerRegistry {

	private final Map<Class, Object> serviceInstances = new HashMap<>();

	@Override
	public <T> T get(Class<T> serviceClass) {
		Objects.requireNonNull(serviceClass, "Service class cannot be null");

		return (T)this.serviceInstances.get(serviceClass);
	}

	@Override
	public <T> void register(Class<T> serviceClass, T serviceIstance) {
		Objects.requireNonNull(serviceClass, "Service class cannot be null");
		Objects.requireNonNull(serviceIstance, "Service instance cannot be null");

		this.serviceInstances.put(serviceClass, serviceIstance);
	}
}
