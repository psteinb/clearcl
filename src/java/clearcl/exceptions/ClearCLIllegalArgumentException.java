package clearcl.exceptions;

/**
 * Exception thrown when kernel argument missing.
 *
 * @author royer
 */
public class ClearCLIllegalArgumentException extends ClearCLException
{

  private static final long serialVersionUID = 1L;

  public ClearCLIllegalArgumentException(String pMessage)
  {
    super(pMessage);
  }

}
