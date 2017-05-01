package clearcl;

import clearcl.abs.ClearCLBase;

/**
 * ClearCLQueue is the ClearCL abstraction for OpenCl queues.
 *
 * @author royer
 */
public class ClearCLQueue extends ClearCLBase
{

  private ClearCLContext mClearCLContext;

  /**
   * This constructor is called internally from an OpenCl context.
   * 
   * @param pClearCLContext
   *          context
   * @param pQueuePointer
   *          queue peer pointer
   */
  public ClearCLQueue(ClearCLContext pClearCLContext,
                      ClearCLPeerPointer pQueuePointer)
  {
    super(pClearCLContext.getBackend(), pQueuePointer);
    mClearCLContext = pClearCLContext;
  }

  /**
   * Returns this queues context.
   * 
   * @return context
   */
  public ClearCLContext getContext()
  {
    return mClearCLContext;
  }

  /**
   * Waits for queue to finish enqueued tasks (such as: kernel execution, buffer
   * and image copies, writes and reads).
   */
  public void waitToFinish()
  {
    getBackend().waitQueueToFinish(getPeerPointer());
  }

  @Override
  public void close()
  {
    if (getPeerPointer() != null)
    {
      getBackend().releaseQueue(getPeerPointer());
      setPeerPointer(null);
    }
  }

}
