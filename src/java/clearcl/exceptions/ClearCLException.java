package clearcl.exceptions;

/**
 * Standard ClearCL exception. Wraps internal backend exception.
 *
 * @author royer
 */
public class ClearCLException extends RuntimeException
{
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a ClearCL exception from a message and a cause.
   * 
   * @param pMessage
   * @param pCause
   */
  public ClearCLException(String pMessage, Throwable pCause)
  {
    super(pMessage, pCause);
  }

  public ClearCLException(String pMessage)
  {
    super(pMessage);
  }

}
