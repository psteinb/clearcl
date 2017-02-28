
//default image_render_slice_3df z=0i
//default image_render_slice_3df vmin=0f
//default image_render_slice_3df vmax=1f
//default image_render_slice_3df gamma=1f
__kernel void image_render_slice_3df(         __read_only   image3d_t  image,
                                      __global __write_only uchar*     rgbabuffer,
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
  
  vstore4((uchar4){bytevalue,bytevalue,bytevalue,255}, i, rgbabuffer);
}


//default image_render_slice_3df z=0i
//default image_render_slice_3df vmin=0f
//default image_render_slice_3df vmax=1f
//default image_render_slice_3df gamma=1f
__kernel void buffer_render_slice_3df(__global              float*     image,
                                      __global __write_only uchar*     rgbabuffer,
                                                            float      vmin,
                                                            float      vmax,
                                                            float      gamma,
                                                            int        z
                                                            )
{
  const int width  = get_global_size(0);
  const int height = get_global_size(1);
  const int depth  = get_global_size(2);
  
  const int x = get_global_id(0);
  const int y = get_global_id(1);
  
  const int ri = x+ width*y;
  const int i = ri + width*height*z;
  
  const float value = clamp(native_powr((image[i]-vmin)/(vmax-vmin),gamma),0.0f,1.0f);
  
  const uchar bytevalue = (uchar)(255*value);

  vstore4((uchar4){bytevalue,bytevalue,bytevalue,255}, ri, rgbabuffer);
}



