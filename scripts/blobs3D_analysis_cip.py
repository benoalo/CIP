#@ImagePlus imp
#@CIP cip

# Author: Benoit Lombardot
# 2017-12-13 : first version
#
# Demonstration of image filtering, segmentation, measures and result visualisation with a 2D image 
# the script can be test with the 3Dblobs image available from sample data

# image processing
impFilt = cip.opening(imp, cip.list(5,5,1) )
impSeg  = cip.watershed(impFilt, 'threshold',500, 'hmin',100 )

# conversion from labelMap to regions
regions = cip.region(impSeg)
nObject = len(regions)
 
measures = cip.measure(regions, cip.list('mean','size'), imp, 'unit', True )
measures_noUnit = cip.measure(regions, cip.list('mean','size'), imp, 'unit', False )

# visualisation of the segmentation and measures
h = cip.show(imp)
cip.show( h, regions, 'spectrum', 'width', 2)
cip.show( measures )
cip.show( measures_noUnit )


print nObject
print cip.spacing( imp )
print (measures['size'][0]/measures_noUnit['size'][0])