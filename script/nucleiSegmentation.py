#@Img img0

#@CIP cip
#@UIService ui

# the script can be used for the segmentation of 2DEmbryoSection.tif and 3DEmbryo_small.tiff

img = cip.duplicate(img0)

# segment the nucleus
imgNuc = cip.opening( img, cip.list(5,5,0) , 'disk' )
imgNuc = cip.watershed(imgNuc, 'T',500 , 'H',1 , 'Method','Binary' )

ui.show(imgNuc)