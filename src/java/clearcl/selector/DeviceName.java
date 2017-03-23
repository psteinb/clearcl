package clearcl.selector;

import java.util.ArrayList;

import clearcl.ClearCLDevice;

/**
 * Selects the first device that contains a string
 *
 * @author royer
 */
public class DeviceName implements DeviceSelector
{

  private String mNameSubString = "";

  /**
   * Instanciates a device name selector with the given substring.
   * 
   * @param pNameSubString
   *          substring
   * @return selector
   */
  public static DeviceSelector subString(String pNameSubString)
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * Constructs a device selector that filters out devices with a certain name
   * (bad devices).
   * 
   * @param pDiscrete
   *          if true select discrete, if false select non-discrete.
   */
  private DeviceName()
  {
    super();
  }

  /**
   * Adds a name to te list of bad devices.
   * 
   * @param pNameSubString
   *          substring that should be contained in device name
   */
  public void setNameSubstring(String pNameSubString)
  {
    mNameSubString = pNameSubString;
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
    return pClearCLDevice.getName()
                         .trim()
                         .toLowerCase()
                         .contains(mNameSubString.trim()
                                                 .toLowerCase());
  }

}
