package be.mrtus.container;

public interface Container {

	public <T> T get(Class<T> serviceClass);
}
