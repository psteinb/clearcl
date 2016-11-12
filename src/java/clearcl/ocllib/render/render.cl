#include [OCLlib] "rgb/rgbhsv.cl" 




//default image_render_2df vmin=0f
//default image_render_2df vmax=1f
//default image_render_2df gamma=1f
__kernel void image_render_2df(          __read_only  image2d_t  image,
                                __global __write_only uchar*     rgbbuffer,
        		                                          float      vmin,
        		                                          float      vmax,
        		                                          float      gamma)
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
    

  const int width = get_image_width(image);
  const int height = get_image_height(image);
  
	int2 pos = {get_global_id(0),get_global_id(1)};
	
	float4 value4 = read_imagef(image, sampler, pos);
	
	float value = clamp(native_powr((value4.x-vmin)/(vmax-vmin),gamma),0.0f,1.0f);
	
  uchar bytevalue = (uchar)(255*value);
	
	int i = (pos.x+ width*pos.y);
  
  vstore4(uchar4(bytevalue,bytevalue,bytevalue,255), i, rgbbuffer);
}


//default image_render_slice_3df z=0i
//default image_render_slice_3df vmin=0f
//default image_render_slice_3df vmax=1f
//default image_render_slice_3df gamma=1f
__kernel void image_render_slice_3df(         __read_only  image3d_t  image,
                                      __global __write_only uchar*     rgbbuffer,
                                                            float      vmin,
                                                            float      vmax,
                                                            float      gamma,
                                                            int        z
                                                            )
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
  
  const int width = get_image_width(image);
  const int height = get_image_height(image);
  
  const int4 pos = {get_global_id(0),get_global_id(1),z,0};
  
  const float4 value4 = read_imagef(image, sampler, pos);
  
  const float value = clamp(native_powr((value4.x-vmin)/(vmax-vmin),gamma),0.0f,1.0f);
  
  const uchar bytevalue = (uchar)(255*value);
  
  const int i = (pos.x+ width*pos.y);
  
  vstore4(uchar4(bytevalue,bytevalue,bytevalue,255), i, rgbbuffer);
}


//default image_render_maxproj_3df vmin=0f
//default image_render_maxproj_3df vmax=1f
//default image_render_maxproj_3df gamma=1f
//default image_render_maxproj_3df zmin=0i
//default image_render_maxproj_3df zmax=16000i
//default image_render_maxproj_3df zstep=1i
__kernel void image_render_maxproj_3df(          __read_only  image3d_t  image,
                                        __global __write_only uchar*     rgbbuffer,
                                                              float      vmin,
                                                              float      vmax,
                                                              float      gamma,
                                                              int        zmin,
                                                              int        zmax,
                                                              int        zstep
                                                              )
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
  
  const int width  = get_image_width(image);
  const int height = get_image_height(image);
  const int depth  = get_image_depth(image);
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  
  zmin = max(0,zmin);
  zmax = min(depth,zmax);
  
  int4 pos = {x,y,0,0};
  float acc = 0;
  for(pos.z=zmin; pos.z<zmax;)
  {
    for(int i=0; i<8; i++)
    {
      const float value = read_imagef(image, sampler, pos).x;
      acc = fmax(acc,value);
      pos.z+=zstep;
    }
  }
  
  const float value = clamp(native_powr((acc-vmin)/(vmax-vmin),gamma),0.0f,1.0f);
  
  const uchar bytevalue = (uchar)(255*value);
  
  const int i = (x+ width*y);
  
  vstore4(uchar4(bytevalue,bytevalue,bytevalue,255), i, rgbbuffer);
}

//default image_render_avgproj_3df vmin=0f
//default image_render_avgproj_3df vmax=1f
//default image_render_avgproj_3df gamma=1f
//default image_render_avgproj_3df zmin=0i
//default image_render_avgproj_3df zmax=16000i
//default image_render_avgproj_3df zstep=1i
__kernel void image_render_avgproj_3df(          __read_only  image3d_t  image,
                                        __global __write_only uchar*     rgbbuffer,
                                                              float      vmin,
                                                              float      vmax,
                                                              float      gamma,
                                                              int        zmin,
                                                              int        zmax,
                                                              int        zstep
                                                              )
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
  
  const int width  = get_image_width(image);
  const int height = get_image_height(image);
  const int depth  = get_image_depth(image);
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  
  zmin = max(0,zmin);
  zmax = min(depth,zmax);
  
  int4 pos = {x,y,0,0};
  float acc = 0;
  for(pos.z=zmin; pos.z<zmax;)
  {
    for(int i=0; i<8; i++)
    {
      const float value = read_imagef(image, sampler, pos).x;
      acc += value;
      pos.z+=zstep;
    }
  }
  
  const float gcvalue = clamp(native_powr(((acc/depth)-vmin)/(vmax-vmin),gamma),0.0f, 1.0f);
  
  const uchar bytevalue = (uchar)(255*gcvalue);
  
  const int i = x+ width*y;
  
  vstore4(uchar4(bytevalue,bytevalue,bytevalue,255), i, rgbbuffer);
}

//default image_render_colorproj_3df vmin=0f
//default image_render_colorproj_3df vmax=1f
//default image_render_colorproj_3df gamma=1f
//default image_render_colorproj_3df zmin=0i
//default image_render_colorproj_3df zmax=16000i
//default image_render_colorproj_3df zstep=1i
__kernel void image_render_colorproj_3df(          __read_only  image3d_t  image,
                                          __global __write_only uchar*     rgbbuffer,
                                                                float      vmin,
                                                                float      vmax,
                                                                float      gamma,
                                                                int        zmin,
                                                                int        zmax,
                                                                int        zstep
                                                                )
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
  
  const int width  = get_image_width(image);
  const int height = get_image_height(image);
  const int depth  = get_image_depth(image);
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  
  const float idepth = 1.0f/depth;
  
  zmin = max(0,zmin);
  zmax = min(depth,zmax);
  
  int4 pos = {x,y,0,0};
  float4 acc = float4(0.0f,0.0f,0.0f,0.0f);
  for(pos.z=zmin; pos.z<zmax;pos.z+=zstep)
  {
    const float value = read_imagef(image, sampler, pos).x;
    const float cvalue = clamp(native_powr(((value)-vmin)/(vmax-vmin),gamma), 0.0f, 1.0f);
    const float h = idepth*pos.z; 
    
    const float4 frgba = hsv2rgb((float4){h,1,0.5*cvalue,1});
    acc = fmax(acc,frgba);
  }
   
  const int i = x+ width*y;
  
  uchar4 rgba = convert_uchar4(255*acc);
  
  //if(x==0 & y==0)
  //  printf("acc = %v4f %#d \n", acc, rgba.x);
  
  vstore4(rgba, i, rgbbuffer);
}


