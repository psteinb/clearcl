package clearcl.selector;

import java.util.ArrayList;

import clearcl.ClearCLDevice;

/**
 * Selects a device based on the amount of global memory.
 *
 * @author royer
 */
public class GlobalMemorySelector implements DeviceSelector
{

  public static GlobalMemorySelector MAX = new GlobalMemorySelector(0);

  public static GlobalMemorySelector ATLEAST(long pAtLeastBytes)
  {
    return new GlobalMemorySelector(pAtLeastBytes);
  };

  private ClearCLDevice mBestDevice;
  private long mAtLeastBytes;

  /**
   * prevents direct instantiation.
   * 
   * @param pAtLeastBytes
   */
  private GlobalMemorySelector(long pAtLeastBytes)
  {
    super();
    mAtLeastBytes = pAtLeastBytes;
  }

  @Override
  public void init(ArrayList<ClearCLDevice> pDevices)
  {
    long lMaxGlobalMemory = 0;
    for (ClearCLDevice lDevice : pDevices)
    {
      {
        long lGlobalMemorySizeInBytes = lDevice.getGlobalMemorySizeInBytes();

        // System.out.println(lDevice);
        // System.out.println("lGlobalMemorySizeInBytes=" +
        // lGlobalMemorySizeInBytes);

        if (lGlobalMemorySizeInBytes > lMaxGlobalMemory && lGlobalMemorySizeInBytes >= mAtLeastBytes)
        {
          mBestDevice = lDevice;
          lMaxGlobalMemory = lGlobalMemorySizeInBytes;
        }
      }
    }

  }

  @Override
  public boolean selected(ClearCLDevice pClearCLDevice)
  {
    return mBestDevice == pClearCLDevice;
  }

}
