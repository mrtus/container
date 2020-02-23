package be.mrtus.container;

public interface RegisterableServicesContainer extends Container {

	public <T> void register(Class<T> serviceClass, T serviceIstance);
}
