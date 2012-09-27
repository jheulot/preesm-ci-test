/**
 Generated by the Orcc C Embedded backend
 from actor "org.ietr.sobel.sdf.Transformer"
*/

#include <stdio.h>
#include <stdlib.h>
#include "orcc_types.h"

////////////////////////////////////////////////////////////////////////////////
// Functions/procedures

////////////////////////////////////////////////////////////////////////////////
// Actions
void Transformer_transform(i32 OFFSET, i32 *data_in, i32 *data_out)
{
	i32 d_in[64];
	i32 d_out[64];
	i32 i;
	i32 tmp_data_in;
	i32 local_OFFSET;
	i32 idx_d_out;
	i32 local_d_out;

	i = 0;
	while (i <= 63) {
		tmp_data_in = data_in[i];
		local_OFFSET = OFFSET;
		d_out[i] = tmp_data_in + local_OFFSET;i = i + 1;
	}
	idx_d_out = 0;
	while (idx_d_out < 64) {
		local_d_out = d_out[idx_d_out];
		data_out[idx_d_out] = local_d_out;idx_d_out = idx_d_out + 1;
	}

}


////////////////////////////////////////////////////////////////////////////////
// Initializes 
 