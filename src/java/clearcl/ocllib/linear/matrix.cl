
// Loads a matrix from a buffer with offset
inline float16 matrix_load(int offset, __constant float* pointer) 
{
    float16 matrix = vload16(offset, pointer);
    return matrix;
}


// 4x4 matrix multiplication:
inline float4 matrix_mult(float16 matrix, float4 vector) 
{
  const float4 result;
  result.x = dot(vector, ((float4)(matrix[0],matrix[1],matrix[2],matrix[3])));
  result.y = dot(vector, ((float4)(matrix[4],matrix[5],matrix[6],matrix[7])));
  result.z = dot(vector, ((float4)(matrix[8],matrix[9],matrix[10],matrix[11])));
  result.w = dot(vector, ((float4)(matrix[12],matrix[13],matrix[14],matrix[15])));    
  return result;             
}

// 4x4 matrix transpose 
inline float16 matrix_transpose(float16 matrix) 
{
  return matrix.s048C159D26AE37BF;
}

/*
// read_imagef with matrix multiplication, int4 version:
inline float4 trans_read_imagef( image3d_t image,
                           sampler_t sampler,
                           float16 matrix,
                           int4 vector)
{
  const int4 transvector = convert_int4(matrix_mult(matrix,convert_float4(vector))); 
  return read_imagef(image, sampler, transvector);
}
/**/

// read_imagef with matrix multiplication, float4 version:
inline float4 trans_read_imagef( image3d_t image,
                                 sampler_t sampler,
                                 float16 matrix,
                                 float4 vector)
{
  vector.w=1;
  const float4 transvector = matrix_mult(matrix,vector); 
  return read_imagef(image, sampler, transvector);
}

