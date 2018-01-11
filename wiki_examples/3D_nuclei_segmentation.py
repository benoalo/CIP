#@ImagePlus img
#@Float blurRadius
#@CIP cip

# CIP example: a simple 3d cell measure. can be run with the image 
# 	3D_nuc_mem_full_DS.tif available on the CIP repository
# Author: Benoit Lombardot

# duplicate the nuclei channel
img_nuc = cip.slice( img , 'dimensions',2 , 'position',0 )

# blurring using the image pixel size
spacing = cip.spacing(img_nuc)
radii = [blurRadius/r for r in spacing]
img_blur = cip.gauss(img_nuc, cip.list(radii) )

# segmentation of the nuclei
threshold = cip.threshold(img_blur, 'moments', 'value')
img_seg = cip.label(img_blur, threshold )

# measure of nuclei size in image unit
regions = cip.region(img_seg, 'nuclei3D_')
measures  = cip.measure( regions , 'size')

# visualize intermediary step and measures
cip.show( img_nuc )
cip.show( img_blur )
cip.show( img_seg, 'glasbey_inverted' )
cip.show( measures )
