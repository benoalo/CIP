#@Dataset img0

#@CIP cip
#@UIService ui

# the script can be used to count the spots in nuclei_transcripts.tif


img = cip.duplicate(img0)

# segment the nucleus
imgNuc = cip.opening( img, 5 )
imgNuc = cip.threshold(imgNuc, 'otsu')

# detect spots
imgSpot = cip.tophat( img, 5 )
imgSpot = cip.gauss( img, 1 )
imgSpot = cip.maxima( imgSpot, 'h', 200 )

# count spot in nucleus
imgNucSpot = cip.mul(imgNuc, imgSpot)
imgNucSpot = cip.label(imgNucSpot, 0.5 )


ui.show(imgNuc)
ui.show(imgNucSpot)