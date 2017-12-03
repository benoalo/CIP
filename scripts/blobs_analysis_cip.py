#@ImagePlus imp
#@CIP cip

impFilt = cip.gauss(imp, 5 )
#impSeg  = cip.label(impFilt, 100)
impSeg  = cip.watershed(impFilt, 100, 10)
#impSeg = cip.maxima(impFilt, 100, 10)
regions = cip.region(impSeg)
measures = cip.measure(regions, cip.list('mean','size'), imp )

h = cip.show(imp)
cip.show( h, regions, 'fire', 'width', 2)
cip.show( measures )




a = [1,2,3]
b = [4,5,6]
c = ['a','b','c']
d = [[1,2],[3,4],[5,6]]
objects = {'a':a , 'test':b, 'c':c , 'd':d}
cip.show( objects , 'my custom measures' )
