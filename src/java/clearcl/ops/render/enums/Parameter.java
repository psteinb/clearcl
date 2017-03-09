package clearcl.ops.render.enums;

public enum Parameter implements ParameterInterface
{
 Max("vmax"),
 Min("vmin"),
 Gamma("gamma"),
 Alpha("alpha"),
 MaxSteps("maxsteps"),
 ProjectionMatrix("invProjectionMatrix"),
 ModelViewMatrix("invModelViewMatrix");

  private final String mKernelArgumentName;

  Parameter(String pKernelArgumentName)
  {
    mKernelArgumentName = pKernelArgumentName;
  }

  @Override
  public String getKernelArgumentName()
  {
    return mKernelArgumentName;
  }

}
