package clearcl.selector;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import clearcl.ClearCLDevice;
import clearcl.benchmark.Benchmark;
import clearcl.util.ClearCLFolder;

/**
 * Selects a device based on the actual computation speed based on benchmarking.
 * Benchmarking results are cached.
 *
 * @author royer
 */
public class FastestDeviceSelector implements DeviceSelector
{

  public static FastestDeviceSelector Fastest = new FastestDeviceSelector();

  private ClearCLDevice mFastestDevice;

  /**
   * prevents direct instantiation.
   */
  private FastestDeviceSelector()
  {
    super();
  }

  @Override
  public void init(ArrayList<ClearCLDevice> pDevices)
  {
    try
    {
      File lClearCLFolder = ClearCLFolder.get();

      File lFastestDeviceFile = new File(lClearCLFolder,
                                         "fastestdevice.txt");

      if (lFastestDeviceFile.exists())
      {
        String lFastestDeviceName = FileUtils.readFileToString(lFastestDeviceFile)
                                             .trim();
        mFastestDevice = getDeviceWithName(pDevices,
                                           lFastestDeviceName);
      }
      else
      {
        mFastestDevice = Benchmark.getFastestDevice(pDevices);
        FileUtils.writeStringToFile(lFastestDeviceFile,
                                    mFastestDevice.getName());
      }
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
  }

  private ClearCLDevice getDeviceWithName(ArrayList<ClearCLDevice> pDevices,
                                          String pDeviceName)
  {
    for (ClearCLDevice lDevice : pDevices)
    {
      if (lDevice.getName().trim().toLowerCase().equals(pDeviceName))
        return lDevice;
    }
    return null;
  }

  @Override
  public boolean selected(ClearCLDevice pClearCLDevice)
  {
    // in case there was a problem, we don't select anything...
    if (mFastestDevice == null)
      return true;

    return mFastestDevice == pClearCLDevice;
  }

}
