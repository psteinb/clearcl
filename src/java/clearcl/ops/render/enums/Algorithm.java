package clearcl.ops.render.enums;

public enum Algorithm implements AlgorithmInterface
{
  MaximumProjection("render/volume/maxproj.cl","image_render_maxproj_3d");

  private final String mKernelPath, mKernelName;

  Algorithm(String pKernelPath, String pKernelName)
  {
    mKernelPath = pKernelPath;
    mKernelName = pKernelName;
  }

  @Override
  public String getKernelPath()
  {
    return mKernelPath;
  }

  @Override
  public String getKernelName()
  {
    return mKernelName;
  }
}
