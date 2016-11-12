package clearcl.abs;

import java.util.concurrent.CopyOnWriteArrayList;

import clearcl.ClearCLPeerPointer;
import clearcl.ClearCLQueue;
import clearcl.backend.ClearCLBackendInterface;
import clearcl.interfaces.ClearCLMemChangeListener;
import clearcl.interfaces.ClearCLMemInterface;

public abstract class ClearCLMemBase extends ClearCLBase implements
                                                        ClearCLMemInterface
{

  private CopyOnWriteArrayList<ClearCLMemChangeListener> mListener = new CopyOnWriteArrayList<>();

  /**
   * Constructs the abstract class for all images and buffers.
   * 
   * @param pClearCLBackendInterface
   * @param pPointer
   */
  public ClearCLMemBase(ClearCLBackendInterface pClearCLBackendInterface,
                        ClearCLPeerPointer pPointer)
  {
    super(pClearCLBackendInterface, pPointer);
  }

  /**
   * Adds an image or buffer change listener.
   * 
   * @param pListener
   *          listener
   */
  public void addListener(ClearCLMemChangeListener pListener)
  {
    mListener.add(pListener);
  }

  /**
   * Removes an image or buffer change listener.
   * 
   * @param pListener
   *          listener
   */
  public void removeListener(ClearCLMemChangeListener pListener)
  {
    mListener.remove(pListener);
  }

  /* (non-Javadoc)
   * @see clearcl.interfaces.ClearCLMemInterface#notifyListenersOfChange(clearcl.ClearCLQueue)
   */
  @Override
  public void notifyListenersOfChange(ClearCLQueue pQueue)
  {
    for (ClearCLMemChangeListener lClearCLMemChangeListener : mListener)
    {
      lClearCLMemChangeListener.change(pQueue, this);
    }
  }

}
