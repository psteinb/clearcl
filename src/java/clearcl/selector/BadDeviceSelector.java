package clearcl.selector;

import java.util.ArrayList;

import clearcl.ClearCLDevice;

/**
 * Selects out bad devices
 *
 * @author royer
 */
public class BadDeviceSelector implements DeviceSelector
{

  public static BadDeviceSelector NotIntegratedIntel = new BadDeviceSelector().addName("HD Graphics")
                                                                              .addName("Iris Graphics")
                                                                              .addName("Iris Pro Graphics");

  public static BadDeviceSelector NotSlowIntegratedIntel = new BadDeviceSelector().addName("HD Graphics");

  public static BadDeviceSelector NotIntegratedIntelHD = new BadDeviceSelector().addName("HD Graphics");

  private ArrayList<String> mBadDeviceNamesList = new ArrayList<>();

  /**
   * Constructs a device selector that filters out devices with a certain name
   * (bad devices).
   * 
   * @param pDiscrete
   *          if true select discrete, if false select non-discrete.
   */
  private BadDeviceSelector()
  {
    super();
  }

  public BadDeviceSelector addName(String pName)
  {
    mBadDeviceNamesList.add(pName);
    return this;
  }

  /* (non-Javadoc)
   * @see clearcl.selector.DeviceSelector#init(java.util.ArrayList)
   */
  @Override
  public void init(ArrayList<ClearCLDevice> pDevices)
  {

  }

  /* (non-Javadoc)
   * @see clearcl.selector.DeviceSelector#selected(clearcl.ClearCLDevice)
   */
  @Override
  public boolean selected(ClearCLDevice pClearCLDevice)
  {
    return !nameContains(pClearCLDevice.getName(),
                         mBadDeviceNamesList);
  }

  private boolean nameContains(String pDeviceName,
                               ArrayList<String> pNameSubStringList)
  {
    pDeviceName = pDeviceName.toLowerCase();
    for (String lDeviceNameSubString : pNameSubStringList)
      if (pDeviceName.contains(lDeviceNameSubString.toLowerCase()))
        return true;
    return false;
  }

}
