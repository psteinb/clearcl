package clearcl.interfaces;

import clearcl.ClearCLContext;
import clearcl.ClearCLQueue;
import clearcl.enums.HostAccessType;
import coremem.interfaces.SizedInBytes;

public interface ClearCLMemInterface extends SizedInBytes
{
  /**
   * Calling this method notifies listeners that the contents of this OpenCL
   * object might have changed.
   * 
   * @param pQueue
   */
  void notifyListenersOfChange(ClearCLQueue pQueue);

  /**
   * Returns ClearCL context associated to this OpenCL mem object.
   * 
   * @return
   */
  public ClearCLContext getContext();
  
  /**
   * Returns host access type
   * 
   * @return host acess type
   */
  public HostAccessType getHostAccessType();

}
