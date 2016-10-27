package clearcl.interfaces;

import clearcl.ClearCLQueue;
import coremem.interfaces.SizedInBytes;

public interface ClearCLMemInterface extends SizedInBytes
{
  void notifyListenersOfChange(ClearCLQueue pQueue);

}
