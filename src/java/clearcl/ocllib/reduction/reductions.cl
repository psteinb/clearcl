__kernel
void reduce_min_buffer_f(__global float* buffer,
                         int    length,
                __global float* result) 
{
  int index  = get_global_id(0);
  int stride = get_global_size(0);
  
  float min = INFINITY;
  float max = -INFINITY;
  
  while(index<length)
  {
    float value = buffer[index];
    min = fmin(min, value);
    max = fmax(max, value);
    index += stride;
  }

  result[2*get_global_id(0)+0] = min;
  result[2*get_global_id(0)+1] = max;
}




__kernel
void reduce_min_image_1df(__read_only image1d_t  image,
                          __global    float*     result) 
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
  
  const int width = get_image_width(image);
  
  int x  = get_global_id(0);
  int stridex = get_global_size(0);
  
  float min = INFINITY;
  float max = -INFINITY;
  
  for(int lx=x; lx<width; lx+=stridex)
  {
    float value = (read_imagef(image, sampler, lx)).x;
    min = fmin(min, value);
    max = fmax(max, value);
  }

  int index = 2*get_global_id(0);

  result[index+0] = min;
  result[index+1] = max;
}


    

__kernel
void reduce_min_image_2df(__read_only image2d_t  image,
                          __global    float*     result) 
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
  
  const int width = get_image_width(image);
  const int height = get_image_height(image);
  
  const int x  = get_global_id(0);
  const int y  = get_global_id(1);
  
  const int stridex = get_global_size(0);
  const int stridey = get_global_size(1);
  
  float min = INFINITY;
  float max = -INFINITY;
  
  for(int ly=y; ly<height; ly+=stridey)
  {
    for(int lx=x; lx<width; lx+=stridex)
    {
      const int2 pos = {lx,ly};
   
      float value = (read_imagef(image, sampler, pos)).x;
      min = fmin(min, value);
      max = fmax(max, value);
    }
  }
  
  int index = 2*(x+stridex*y);
  
  result[index+0] = min;
  result[index+1] = max;
}



    
__kernel
void reduce_min_image_3df(__read_only image3d_t  image,
                          __global    float*     result) 
{
  const sampler_t sampler = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP_TO_EDGE | CLK_FILTER_NEAREST;
  
  const int width   = get_image_width(image);
  const int height  = get_image_height(image);
  const int depth   = get_image_depth(image);
  
  const int x       = get_global_id(0);
  const int y       = get_global_id(1);
  const int z       = get_global_id(2);
  
  const int stridex = get_global_size(0);
  const int stridey = get_global_size(1);
  const int stridez = get_global_size(2);
  
  float min = INFINITY;
  float max = -INFINITY;
  
  for(int lz=z; lz<depth; lz+=stridez)
  {
    for(int ly=y; ly<height; ly+=stridey)
    {
      for(int lx=x; lx<width; lx+=stridex)
      {
        const int4 pos = {lx,ly,lz,0};
     
        float value = (read_imagef(image, sampler, pos)).x;
        min = fmin(min, value);
        max = fmax(max, value);
      }
    }
  }

  int index = 2*(x+stridex*y+stridex*stridey*z);
  
  result[index+0] = min;
  result[index+1] = max;
}