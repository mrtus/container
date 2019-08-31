package be.mrtus.container.simple;

import be.mrtus.container.Container;

public class NullContainer implements Container {

	@Override
	public <T> T get(Class<T> serviceClass) {
		return null;
	}
}
