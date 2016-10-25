package clearcl.exceptions;

/**
 * Exception thrown when a stumbling on a unsupported functionality (typically a
 * backend does not support something)
 *
 * @author royer
 */
public class ClearCLUnsupportedException extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  public ClearCLUnsupportedException()
  {
    super("Unsupported OpenCL functionality");
  }

  public ClearCLUnsupportedException(String pMessage)
  {
    super("Unsupported OpenCL functionality: '" + pMessage + "'");
  }

}
