package clearcl.abs;

import clearcl.ClearCLPeerPointer;
import clearcl.backend.ClearCLBackendInterface;

public abstract class ClearCLMemBase extends ClearCLBase
{

  public ClearCLMemBase(ClearCLBackendInterface pClearCLBackendInterface,
                        ClearCLPeerPointer pPointer)
  {
    super(pClearCLBackendInterface, pPointer);
  }

}
