package clearcl.exceptions;

/**
 * Exception thrown when trying to access an image or buffer that is not
 * declared at creation time as being readable or writable from he host.
 *
 * @author royer
 */
public class ClearCLHostAccessException extends ClearCLException
{

  public ClearCLHostAccessException(String pMessage)
  {
    super(pMessage);
  }

}
