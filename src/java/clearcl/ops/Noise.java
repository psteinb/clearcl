package clearcl.ops;

import java.io.IOException;

import clearcl.ClearCLBuffer;
import clearcl.ClearCLKernel;
import clearcl.ClearCLProgram;
import clearcl.ClearCLQueue;
import clearcl.enums.BuildStatus;
import clearcl.ocllib.OCLlib;

public class Noise extends OpsBase
{

  private ClearCLKernel mPerlin2D,mPerlin3D;
  
  private float sx=1,sy=1,sz=1;
  private float ox=0,oy=0,oz=0;

  private int mSeed;

  public Noise(ClearCLQueue pClearCLQueue) throws IOException
  {
    super(pClearCLQueue);

    ClearCLProgram lNoiseProgram =
                                      getClearCLContext().createProgram(OCLlib.class,
                                                                        "noise/noise.cl");
    //lNoiseProgram.addBuildOptionAllMathOpt();
    lNoiseProgram.buildAndLog();
    System.out.println(lNoiseProgram.getSourceCode());
    
   
    mPerlin2D = lNoiseProgram.createKernel("perlin2d");
    mPerlin3D = lNoiseProgram.createKernel("perlin3d");
  }
  
  public void setSeed(int pSeed)
  {
    mSeed = pSeed;
  }
  
  private void setArguments(ClearCLKernel pNoiseKernel, ClearCLBuffer pBuffer)
  {
    pNoiseKernel.setArgument("output", pBuffer);
    pNoiseKernel.setArgument("sx", 100*sx/pBuffer.getWidth());
    pNoiseKernel.setArgument("sy", 100*sy/pBuffer.getHeight());
    pNoiseKernel.setOptionalArgument("sz", sz);
    pNoiseKernel.setArgument("ox", ox);
    pNoiseKernel.setArgument("oy", oy);
    pNoiseKernel.setOptionalArgument("oz", oz);
    pNoiseKernel.setOptionalArgument("seed", mSeed);
  }

  public void perlin2D(ClearCLBuffer pBuffer, boolean pBlockingRun)
  {
    long lWidth = pBuffer.getWidth();
    long lHeight = pBuffer.getHeight();
    
    setArguments(mPerlin2D,pBuffer);
    
    mPerlin2D.setGlobalSizes(lWidth,lHeight);
    mPerlin2D.run(pBlockingRun);

    pBuffer.notifyListenersOfChange(getQueue());
  }
  
  public void perlin3D(ClearCLBuffer pBuffer, boolean pBlockingRun)
  {
    long lWidth = pBuffer.getWidth();
    long lHeight = pBuffer.getHeight();
    long lDepth = pBuffer.getDepth();
    
    setArguments(mPerlin3D,pBuffer);
    
    mPerlin3D.setGlobalSizes(lWidth,lHeight,lDepth);
    mPerlin3D.run(pBlockingRun);

    pBuffer.notifyListenersOfChange(getQueue());
  }
  
}
