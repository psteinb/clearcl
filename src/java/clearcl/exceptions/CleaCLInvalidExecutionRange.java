package clearcl.exceptions;

/**
 * Exception thrown when the execution range ( global/local sizes or offsets)
 * are invalid or undefined.
 *
 * @author royer
 */
public class CleaCLInvalidExecutionRange extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  public CleaCLInvalidExecutionRange(String pMessage)
  {
    super(pMessage);
  }
}
