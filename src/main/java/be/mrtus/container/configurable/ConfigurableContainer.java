package be.mrtus.container.configurable;

import be.mrtus.container.Container;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface ConfigurableContainer extends Container {

	public void addServiceProvider(Class<? extends ServiceProvider> serviceProviderClass);

	default public void addServiceProvider(ServiceProvider serviceProvider) {
		serviceProvider.configure(this);
	}

	public <T> void handle(Class<T> inflectorClass, Consumer<T> consumer);

	public <T> void share(Class<T> serviceClass, Supplier<T> supplier);

	default public <T> void share(Class<T> serviceClass, Class<? extends T> serviceImplementationClass) {
		this.share(serviceClass, () -> this.get(serviceImplementationClass));
	}
}
