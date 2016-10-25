package clearcl.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLContext;
import clearcl.ClearCLDevice;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.enums.BuildStatus;
import clearcl.enums.DataType;
import clearcl.enums.HostAccessType;
import clearcl.enums.KernelAccessType;

public class Benchmark
{
  private static final int cImageSize = 4 * 1024;
  private static final int cRepeats = 128;

  public static ClearCLDevice getFastestDevice(ArrayList<ClearCLDevice> pDevices)
  {
    try
    {
      ClearCLDevice lFastestDevice = null;
      double lMinElapsedTime = Double.POSITIVE_INFINITY;

      for (ClearCLDevice lDevice : pDevices)
      {
        double lElapsedTimeInSeconds = executeBenchmarkOnDevice(lDevice,
                                                                cRepeats);

        System.out.format("Device: %s elapsed time: %g ms \n",
                          lDevice.getName(),
                          lElapsedTimeInSeconds);

        if (lElapsedTimeInSeconds < lMinElapsedTime)
        {
          lMinElapsedTime = lElapsedTimeInSeconds;
          lFastestDevice = lDevice;
        }
      }

      return lFastestDevice;
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }
  }

  public static double executeBenchmarkOnDevice(ClearCLDevice pClearClDevice,
                                                int pRepeats) throws IOException
  {
    ClearCLContext lContext = pClearClDevice.createContext();

    ClearCLProgram lProgram = lContext.createProgram(Benchmark.class,
                                                     "kernel/benchmark.cl");
    lProgram.addBuildOptionAllMathOpt();

    long lStartCompileTimeNanos = System.nanoTime();
    BuildStatus lBuildStatus = lProgram.build();
    if (lBuildStatus != BuildStatus.Success)
    {
      System.out.println(lBuildStatus);
      System.out.println(lProgram.getBuildLog());
    }
    long lStopCompileTimeNanos = System.nanoTime();
    double lCompileElapsedTime = TimeUnit.MICROSECONDS.convert((lStopCompileTimeNanos - lStartCompileTimeNanos),
                                                               TimeUnit.NANOSECONDS) / pRepeats;
    System.out.format("Compilation time: %g us \n",
                      lCompileElapsedTime);

    ClearCLBuffer lBufferA = lContext.createBuffer(HostAccessType.NoAccess,
                                                   KernelAccessType.ReadOnly,
                                                   DataType.Float,
                                                   cImageSize * cImageSize);
    ClearCLBuffer lBufferB = lContext.createBuffer(HostAccessType.NoAccess,
                                                   KernelAccessType.ReadWrite,
                                                   DataType.Float,
                                                   cImageSize * cImageSize);

    ClearCLKernel lKernelFill = lProgram.createKernel("fill");
    lKernelFill.setGlobalSizes(cImageSize, cImageSize);

    lKernelFill.setArguments(lBufferA, lBufferB);
    lKernelFill.run();

    ClearCLKernel lKernelCompute = lProgram.createKernel("benchmark1");
    lKernelCompute.setGlobalSizes(cImageSize, cImageSize);
    lKernelCompute.setArguments(lBufferA, lBufferB);

    long lStartTimeNanos = System.nanoTime();
    for (int r = 0; r < pRepeats; r++)
    {
      lKernelCompute.run(false);
    }
    lContext.getDefaultQueue().waitToFinish();
    long lStopTimeNanos = System.nanoTime();

    double lElapsedTime = TimeUnit.MILLISECONDS.convert((lStopTimeNanos - lStartTimeNanos),
                                                        TimeUnit.NANOSECONDS) / pRepeats;

    return lElapsedTime;
  }
}
