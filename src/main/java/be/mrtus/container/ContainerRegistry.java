package be.mrtus.container;

public interface ContainerRegistry extends Container {

	public <T> void register(Class<T> serviceClass, T serviceIstance);
}
