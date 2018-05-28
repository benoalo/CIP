
#@ImagePlus imp0
#@CIP cip

# Description: the script segment object, track them and visualize the resulting track and tracking data
# the script can be run with Tracks for Trackmate sample data in Fiji
# 
# Author: Benoit Lombardot
# 2018-05-28: version 0.1
#

maxT = imp0.getDimensions()[4]

#########################
# detect objects ########
radii = cip.list(1,1,0)
print radii
mask = cip.gauss( imp0, radii )
mask = cip.threshold( mask, 'otsu' )

labelmaps = []
for t in range(maxT) :
	img = cip.slice( mask , 2, t)
	labelmaps.append( cip.label(img, 0.5) )


########################
# track regions ########
radius=10.0
gap_frame = 2
gap_radius = 15
do_split=True
tracksdata, trackmateModel = cip.track(labelmaps, radius, gap_frame, gap_radius, 'split', do_split,'output','all')

#########################
# visualize results #####
cip.show( trackmateModel, imp0, 'mode', 'image')
cip.show( tracksdata, "tracks data" )
cip.show( mask )

