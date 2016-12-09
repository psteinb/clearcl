package clearcl.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLImage;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.BenchmarkTest;
import clearcl.enums.BuildStatus;
import clearcl.enums.DeviceType;
import clearcl.enums.HostAccessType;
import clearcl.enums.ImageChannelDataType;
import clearcl.enums.ImageChannelOrder;
import clearcl.enums.KernelAccessType;
import clearcl.enums.MemAllocMode;
import coremem.enums.NativeTypeEnum;

/**
 * This class provides a static methods for benchmarlking devices.
 *
 * @author royer
 */
public class Benchmark
{
  public static boolean sStdOutVerbose = false;

  private static final int c2DBufferSize = 1024;
  private static final int c3DImageSize = 320;
  private static final int cRepeats = 10;

  /**
   * Returns the fastest device for a given benchmark test.
   * 
   * @param pDevices
   *          list of devices
   * @param pBenchmarkTest
   *          benchmark type.
   * @return
   */
  public static ClearCLDevice getFastestDevice(ArrayList<ClearCLDevice> pDevices,
                                               BenchmarkTest pBenchmarkTest)
  {

    ClearCLDevice lFastestDevice = null;
    double lMinElapsedTime = Double.POSITIVE_INFINITY;

    format("IMPORTANT: Benchmarking available OpenCl devices, please wait \n");

    for (ClearCLDevice lDevice : pDevices)
    {

      println("_______________________________________________________________________");
      println(lDevice.getInfoString());

      double lElapsedTimeInSeconds;

      try
      {
        lElapsedTimeInSeconds = executeBenchmarkOnDevice(lDevice,
                                                         pBenchmarkTest,
                                                         cRepeats);

        format("---> Elapsed time: %g ms \n",
               lElapsedTimeInSeconds);/**/

        if (lElapsedTimeInSeconds < lMinElapsedTime)
        {
          lMinElapsedTime = lElapsedTimeInSeconds;
          lFastestDevice = lDevice;
        }
      }
      catch (Throwable e)
      {
        e.printStackTrace();
      }

    }

    println("_______________________________________________________________________");
    format("fastest device: %s \n", lFastestDevice);

    return lFastestDevice;

  }

  /**
   * Computes the time needed to execute a given bencmark test on a particular
   * device with a given number of repetitions.
   * 
   * @param pClearClDevice
   *          device
   * @param pBenchmarkTest
   *          benchmark test
   * @param pRepeats
   *          nb of repeats
   * @return elapsed time in ms
   * @throws IOException
   */
  public static double executeBenchmarkOnDevice(ClearCLDevice pClearClDevice,
                                                BenchmarkTest pBenchmarkTest,
                                                int pRepeats) throws IOException
  {
    if (pClearClDevice.getType().isCPU())
      return Double.POSITIVE_INFINITY;

    ClearCLContext lContext = pClearClDevice.createContext();

    ClearCLProgram lProgram =
                            lContext.createProgram(Benchmark.class,
                                                   "kernel/benchmark.cl");
    lProgram.addBuildOptionAllMathOpt();

    long lStartCompileTimeNanos = System.nanoTime();
    BuildStatus lBuildStatus = lProgram.buildAndLog();

    long lStopCompileTimeNanos = System.nanoTime();
    double lCompileElapsedTime = TimeUnit.MICROSECONDS.convert(
                                                               (lStopCompileTimeNanos
                                                                - lStartCompileTimeNanos),
                                                               TimeUnit.NANOSECONDS)
                                 / pRepeats;
    /*System.out.format("Compilation time: %g us \n",
                      lCompileElapsedTime);/**/

    ClearCLBuffer lBufferA =
                           lContext.createBuffer(MemAllocMode.None,
                                                 HostAccessType.NoAccess,
                                                 KernelAccessType.ReadOnly,
                                                 NativeTypeEnum.Float,
                                                 c2DBufferSize * c2DBufferSize);
    ClearCLBuffer lBufferB =
                           lContext.createBuffer(MemAllocMode.None,
                                                 HostAccessType.NoAccess,
                                                 KernelAccessType.ReadWrite,
                                                 NativeTypeEnum.Float,
                                                 c2DBufferSize * c2DBufferSize);

    ClearCLBuffer lBufferC =
                           lContext.createBuffer(MemAllocMode.None,
                                                 HostAccessType.NoAccess,
                                                 KernelAccessType.ReadWrite,
                                                 NativeTypeEnum.UnsignedInt,
                                                 c2DBufferSize * c2DBufferSize);

    ClearCLImage lImage = lContext.createImage(MemAllocMode.None,
                                               HostAccessType.NoAccess,
                                               KernelAccessType.ReadWrite,
                                               ImageChannelOrder.R,
                                               ImageChannelDataType.UnsignedInt16,
                                               c3DImageSize,
                                               c3DImageSize,
                                               c3DImageSize);

    ClearCLKernel lKernelCompute;

    switch (pBenchmarkTest)
    {

    case Image:
      lKernelCompute = lProgram.createKernel("image");
      lKernelCompute.setGlobalSizes(c2DBufferSize, c2DBufferSize);
      lKernelCompute.setArguments(lImage, c3DImageSize, lBufferC);
      break;

    default:
    case Buffer:
      lKernelCompute = lProgram.createKernel("buffer");
      lKernelCompute.setGlobalSizes(c2DBufferSize, c2DBufferSize);
      lKernelCompute.setArguments(lBufferA, lBufferB);
      break;

    }

    lContext.getDefaultQueue().waitToFinish();
    long lStartTimeNanos = System.nanoTime();
    for (int r = 0; r < pRepeats; r++)
    {
      lKernelCompute.run(false);
    }
    lContext.getDefaultQueue().waitToFinish();
    long lStopTimeNanos = System.nanoTime();

    double lElapsedTimeNanos = ((double) (lStopTimeNanos
                                          - lStartTimeNanos))
                               / pRepeats;

    double lElapsedTimeInMs = lElapsedTimeNanos * 1e-6;

    lBufferA.close();
    lBufferB.close();
    lBufferC.close();
    lImage.close();

    return lElapsedTimeInMs;
  }

  private static void println(String pString)
  {
    if (sStdOutVerbose)
      System.out.println(pString);
  }

  private static void format(String format, Object... args)
  {
    if (sStdOutVerbose)
      System.out.format(format, args);
  }

}
