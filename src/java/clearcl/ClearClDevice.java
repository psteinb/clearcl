package clearcl;

import clearcl.abs.ClearCLBase;
import clearcl.enums.DeviceInfo;
import clearcl.enums.DeviceType;

/**
 * ClearCLDevice is the ClearCL abstraction for OpenCl devices.
 *
 * @author royer
 */
public class ClearCLDevice extends ClearCLBase
{

  private ClearCLPlatform mClearCLPlatform;
  private ClearCLPeerPointer mDevicePointer;

  /**
   * Construction of this object is done from within a ClearCLPlatform.
   * 
   * @param pClearCLPlatform
   * @param pDevicePointer
   */
  ClearCLDevice(ClearCLPlatform pClearCLPlatform,
                ClearCLPeerPointer pDevicePointer)
  {
    super(pClearCLPlatform.getBackend(), pDevicePointer);
    mClearCLPlatform = pClearCLPlatform;
    mDevicePointer = pDevicePointer;
  }

  /**
   * Returns device name.
   * 
   * @return device name.
   */
  public String getName()
  {
    return getBackend().getDeviceName(mDevicePointer);
  }

  /**
   * Returns device type.
   * 
   * @return device type
   */
  public DeviceType getType()
  {
    return getBackend().getDeviceType(mDevicePointer);
  }

  /**
   * Returns OpenCL version
   * 
   * @return OpenCL version
   */
  public double getVersion()
  {
    String lStringVersion = getBackend().getDeviceVersion(mDevicePointer)
                                        .replace("OpenCL C", "")
                                        .trim();
    Double lDoubleVersion = Double.parseDouble(lStringVersion);
    return lDoubleVersion;
  }

  /**
   * Returns device OpenL extensions string.
   * 
   * @return
   */
  public String getExtensions()
  {
    return getBackend().getDeviceExtensions(mDevicePointer);
  }

  /**
   * Returns this device global memory size in bytes.
   * 
   * @return global memory size in bytes
   */
  public long getGlobalMemorySizeInBytes()
  {
    return getBackend().getDeviceInfo(mDevicePointer,
                                      DeviceInfo.MaxGlobalMemory);
  }

  /**
   * Returns this device max memory allocation size.
   * 
   * @return max allocation size
   */
  public long getMaxMemoryAllocationSizeInBytes()
  {
    return getBackend().getDeviceInfo(mDevicePointer,
                                      DeviceInfo.MaxMemoryAllocationSize);
  }

  /**
   * Returns this device local memory size.
   * 
   * @return local memory size
   */
  public long getLocalMemorySizeInBytes()
  {
    return getBackend().getDeviceInfo(mDevicePointer,
                                      DeviceInfo.LocalMemSize);
  }

  /**
   * Returns this device clock frequency.
   * 
   * @return clock frequency in MHz
   */
  public long getClockFrequency()
  {
    return getBackend().getDeviceInfo(mDevicePointer,
                                      DeviceInfo.MaxClockFreq);
  }

  /**
   * Returns this device number of compute units.
   * 
   * @return number of compute units
   */
  public long getNumberOfComputeUnits()
  {
    return getBackend().getDeviceInfo(mDevicePointer,
                                      DeviceInfo.ComputeUnits);
  }

  /**
   * Returns device info string.
   * 
   * @return device info string
   */
  public String getInfoString()
  {
    StringBuilder lStringBuilder = new StringBuilder();

    lStringBuilder.append(String.format("Device name: %s, type: %s, OpenCL version: %g \n max global memory: %d \n max local memory: %d \n clock freq: %dMhz \n nb compute units: %d \n extensions: %s  \n",
                                        getName(),
                                        getType(),
                                        getVersion(),
                                        getGlobalMemorySizeInBytes(),
                                        getLocalMemorySizeInBytes(),
                                        getClockFrequency(),
                                        getNumberOfComputeUnits(),
                                        getExtensions()));

    return lStringBuilder.toString();
  }

  /**
   * Creates device context.
   * 
   * @return context
   */
  public ClearCLContext createContext()
  {
    ClearCLPeerPointer lContextPointer = getBackend().getContextPeerPointer(mClearCLPlatform.getPeerPointer(),
                                                                            mDevicePointer);
    return new ClearCLContext(this, lContextPointer);
  }

  /* (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString()
  {
    return String.format("ClearCLDevice [mClearCLPlatform=%s, name=%s]",
                         mClearCLPlatform.toString(),
                         getName());
  }

  /* (non-Javadoc)
   * @see clearcl.ClearCLBase#close()
   */
  @Override
  public void close()
  {
    getBackend().releaseDevice(getPeerPointer());
    setPeerPointer(null);
  }

}
