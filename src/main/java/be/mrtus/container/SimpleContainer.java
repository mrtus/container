package be.mrtus.container;

import java.util.HashMap;
import java.util.Map;

final public class SimpleContainer implements ContainerRegistry {

	private final Map<Class, Object> serviceInstances = new HashMap<>();

	@Override
	public <T> T get(Class<T> serviceClass) {
		return (T)this.serviceInstances.get(serviceClass);
	}

	@Override
	public <T> void register(Class<T> serviceClass, T serviceIstance) {
		this.serviceInstances.put(serviceClass, serviceIstance);
	}
}
