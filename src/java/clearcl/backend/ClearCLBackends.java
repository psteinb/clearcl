package clearcl.backend;

import clearcl.backend.javacl.ClearCLBackendJavaCL;
import clearcl.backend.jocl.ClearCLBackendJOCL;
import clearcl.util.OsCheck;

/**
 * Static methods to get the best ClearCL backend.
 *
 * @author royer
 */
public class ClearCLBackends
{

  public static boolean sStdOutVerbose = false;

  /**
   * Returns the best backend. The definition of best means: i) compatible with
   * the OS and OS version. ii) offers the highest OpenCL version. iii) Highest
   * compatibility with CoreMem and native memory access.
   * 
   * @return best ClearCL backend available.
   */
  public static final ClearCLBackendInterface getBestBackend()
  {
    ClearCLBackendInterface lClearCLBackend;

    switch (OsCheck.getOperatingSystemType())
    {
    case Linux:
      print("Linux");
      lClearCLBackend = new ClearCLBackendJOCL();
      break;
    case MacOS:
      print("MacOS");
      lClearCLBackend = new ClearCLBackendJOCL();
      break;
    case Windows:
      print("Windows");
      lClearCLBackend = new ClearCLBackendJavaCL();
      break;
    case Other:
      print("Other");
      lClearCLBackend = new ClearCLBackendJOCL();
      break;
    default:
      print("Unknown");
      lClearCLBackend = new ClearCLBackendJOCL();
      break;
    }
    println(" --> Using backend: "
            + lClearCLBackend.getClass().getSimpleName());

    return lClearCLBackend;
  }

  
  
  private static void print(String pString)
  {
    if (sStdOutVerbose)
      System.out.print(pString);
  }

  private static void println(String pString)
  {
    if (sStdOutVerbose)
      System.out.println(pString);
  }
}
