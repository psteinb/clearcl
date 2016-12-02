package clearcl.exceptions;

/**
 * Exception thrown when the execution range ( global/local sizes or offsets)
 * are invalid or undefined.
 *
 * @author royer
 */
public class ClearCLInvalidExecutionRange extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  public ClearCLInvalidExecutionRange(String pMessage)
  {
    super(pMessage);
  }
}
