package clearcl.backend;

import clearcl.backend.jocl.ClearCLBackendJOCL;

/**
 * Static methods to get the best ClearCL backend.
 *
 * @author royer
 */
public class ClearCLBackends
{
  /**
   * Returns the best backend. The definition of best means: i) compatible with
   * the OS and OS version. ii) offers the highest OpenCL version. iii) Highest
   * compatibility with CoreMem and native memory access.
   * 
   * @return best ClearCL backend available.
   */
  public static final ClearCLBackendJOCL getBestBackend()
  {
    ClearCLBackendJOCL lClearCLBackendJOCL = new ClearCLBackendJOCL();
    return lClearCLBackendJOCL;
  }
}
