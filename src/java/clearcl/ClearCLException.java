package clearcl;

public class ClearCLException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ClearCLException(String pMessage, Throwable pCause)
	{
		super(pMessage,pCause);
	}
	
}
