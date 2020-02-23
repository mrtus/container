package be.mrtus.container.configurable;

import be.mrtus.container.Container;

public class NullContainer implements Container {

	@Override
	public <T> T get(Class<T> serviceClass) {
		return null;
	}
}
