#@ImagePlus img
#@CIP cip

# the script demonstrate how to count spots in a larger object such as a nuclei
# the script can be used with nucleus_spots.tif

# segment the nucleus
imgNuc = cip.opening( img, 5 )
imgNuc = cip.threshold(imgNuc, 'otsu')
nucleus = cip.region(imgNuc)

# detect spots
imgSpot = cip.tophat( img, 5 )
imgSpot = cip.gauss( img, 1 )
imgSpot = cip.maxima( imgSpot, 'h', 200 )

# count spot in nucleus
imgNucSpot = cip.mul(imgNuc, imgSpot)
imgNucSpot = cip.label(imgNucSpot, 0.5 )
spotsInNuc = cip.region(imgNucSpot, 'spot_')
nSpots = len(spotsInNuc)

# visualize the results
h = cip.show(img)
cip.show( nucleus, 'color' ,'white', 'width',2 )
cip.show( spotsInNuc, 'color' ,'spectrum' )

cip.show('the number of spot is ' + str(nSpots) )