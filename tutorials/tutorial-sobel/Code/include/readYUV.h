/*
	============================================================================
	Name        : readYUV.h
	Author      : kdesnos & mpelcat
	Version     : 1.0
	Copyright   : CECILL-C
	Description : Actor code to read a file from the hard drive
	============================================================================
*/

#ifndef READ_YUV_H
#define READ_YUV_H

#define PATH "./akiyo_cif.yuv"
//#define PATH "D:/Temp/BigBuckBunny_1920_1080_24fps.yuv"

#define NB_FRAME 300

/**
* Initialize the readYUV actor.
* Open the YUV file at the given PATH and check its size.
*
* @param xSize
*        The width of the opened YUV file
* @param ySize
*        The heigth of the opened YUV file
*/
void initReadYUV(int xSize, int ySize);

/**
* Read a new frame from the YUV file.
*
* @param xSize
*        The width of the opened YUV file
* @param ySize
*        The heigth of the opened YUV file
* param y
*       Destination of the Y component read from the file
* param u
*       Destination of the U component read from the file
* param v
*       Destination of the V component read from the file
*/
void readYUV(int xSize, int ySize, unsigned char *y, unsigned char *u, unsigned char *v);

#endif
