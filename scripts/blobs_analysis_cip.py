#@ImagePlus imp
#@CIP cip

# Author: Benoit Lombardot
# 2017-12-13 : first version
#
# Demonstration of image filtering, segmentation, measures and result visualisation with a 2D image 
# the script can be test with the blobs image available from sample data

# image processing
impFilt = cip.gauss(imp, 5 )
impSeg  = cip.watershed(impFilt, 100, 10)

# conversion from labelMap to regions
regions = cip.region(impSeg) 
measures = cip.measure(regions, cip.list('mean','size'), imp )

# visualisation of the segmentation and measures
h = cip.show(imp)
cip.show( h, regions, 'fire', 'width', 2)
cip.show( measures )




