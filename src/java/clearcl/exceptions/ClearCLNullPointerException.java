package clearcl.exceptions;

/**
 * Exception thrown when a OpenCL function returns a null pointer.
 *
 * @author royer
 */
public class ClearCLNullPointerException extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  public ClearCLNullPointerException()
  {
    super("Opencl function returned null pointer.");
  }

}
