package be.mrtus.container;

public class ServiceNotFound extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ServiceNotFound(String message) {
		super(message);
	}

	public ServiceNotFound(Exception exception) {
		super(exception);
	}
}
