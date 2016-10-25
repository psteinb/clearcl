package clearcl.exceptions;

public class ClearCLProgramNotBuiltException extends ClearCLException
{
  private static final long serialVersionUID = 1L;

  public ClearCLProgramNotBuiltException()
  {
    super("Program must be built before creating kernels");
  }
}
