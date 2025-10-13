package co.edu.udistrital.mdp.back.exceptions;

/*
 * Excepción que se lanza cuando en el proceso de búsqueda no se encuenta una entidad
 */
public class EntityNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EntityNotFoundException(String message) {
		super(message);
	}
}