package co.edu.udistrital.mdp.back.exceptions;

/*
 * Excepción que se lanza cuando se realiza una operación ilegal
 */
public class IllegalOperationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public IllegalOperationException(String message) {
		super(message);
	}
}
