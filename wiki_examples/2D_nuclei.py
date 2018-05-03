#@ImagePlus img
#@CIP cip

# CIP example: a simple 2d cell measure. can be run with the image 
# 	2D_nuclei.tif available on the CIP repository
# Author: Benoit Lombardot

# crop the image
origin = cip.list(300,200)
size = cip.list(150,150)
img_dup = cip.duplicate(img , origin, size)

# filter and segment the nuclei
img_filt = cip.opening( img_dup , 5 )
img_seg  = cip.watershed( img_filt , 'threshold', 500 , 'hmin', 300)

# measure nuclei size
regions = cip.region(img_seg, 'nuclei')
measures  = cip.measure( regions , 'size')

# visualize intermediary steps
cip.show(img_dup)
cip.show(img_filt)
cip.show(img_seg, 'glasbey')

# visualize results 
cip.show(img_dup)
cip.show( regions, 'color', 'glasbey' ) # show the region on current image
cip.show(measures)
