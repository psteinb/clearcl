#pragma OPENCL EXTENSION cl_khr_3d_image_writes : enable

#pragma OPENCL EXTENSION cl_amd_printf : enable

#pragma OPENCL EXTENSION cl_khr_byte_addressable_store : enable

#ifndef M_PI
    #define   M_PI 3.14159265358979323846f /* pi */
#endif

#ifndef M_LOG2E
    #define   M_LOG2E   1.4426950408889634074f /* log_2 e */
#endif
 
#ifndef M_LOG10E
    #define   M_LOG10E   0.43429448190325182765f /* log_10 e */
#endif
 
#ifndef M_LN2
    #define   M_LN2   0.69314718055994530942f  /* log_e 2 */
#endif

#ifndef M_LN10
    #define   M_LN10   2.30258509299404568402f /* log_e 10 */
#endif

#ifndef BUFFER_READ_WRITE
    #define BUFFER_READ_WRITE 1
inline ushort2 read_buffer3dui(int read_buffer_width, int read_buffer_height, __global ushort * buffer_var, sampler_t sampler, int4 pos )
{
    return (ushort2){buffer_var[pos.x + pos.y * read_buffer_width + pos.z * read_buffer_width * read_buffer_height],0};
}

inline float2 read_buffer3df(int read_buffer_width, int read_buffer_height, __global float* buffer_var, sampler_t sampler, int4 pos )
{
    return (float2){buffer_var[pos.x + pos.y * read_buffer_width + pos.z * read_buffer_width * read_buffer_height],0};
}

inline void write_buffer3dui(int write_buffer_width, int write_buffer_height, __global ushort * buffer_var, int4 pos, ushort value )
{
    buffer_var[pos.x + pos.y * write_buffer_width + pos.z * write_buffer_width * write_buffer_height] = value;
}

inline void write_buffer3df(int write_buffer_width, int write_buffer_height, __global float* buffer_var, int4 pos, float value )
{
    buffer_var[pos.x + pos.y * write_buffer_width + pos.z * write_buffer_width * write_buffer_height] = value;
}

inline ushort2 read_buffer2dui(int read_buffer_width, int read_buffer_height, __global ushort * buffer_var, sampler_t sampler, int2 pos )
{
    return (ushort2){buffer_var[pos.x + pos.y * read_buffer_width ],0};
}

inline float2 read_buffer2df(int read_buffer_width, int read_buffer_height, __global float* buffer_var, sampler_t sampler, int2 pos )
{
    return (float2){buffer_var[pos.x + pos.y * read_buffer_width ],0};
}

inline void write_buffer2dui(int write_buffer_width, int write_buffer_height, __global ushort * buffer_var, int2 pos, ushort value )
{
    buffer_var[pos.x + pos.y * write_buffer_width ] = value;
}

inline void write_buffer2df(int write_buffer_width, int write_buffer_height, __global float* buffer_var, int2 pos, float value )
{
    buffer_var[pos.x + pos.y * write_buffer_width ] = value;
}


inline int get_bufferf_width(int size, __global float* buffer_var )
{
    return size;
}
inline int get_bufferui_width(int size, __global ushort* buffer_var )
{
    return size;
}
inline int get_bufferf_height(int size, __global float* buffer_var )
{
    return size;
}
inline int get_bufferui_height(int size, __global ushort* buffer_var )
{
    return size;
}
inline int get_bufferf_depth(int size, __global float* buffer_var )
{
    return size;
}
inline int get_bufferui_depth(int size, __global ushort* buffer_var )
{
    return size;
}
#endif