package be.mrtus.container.simple;

public class CouldNotInstantiateServiceProvider extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CouldNotInstantiateServiceProvider(Exception exception) {
		super(exception);
	}
}
