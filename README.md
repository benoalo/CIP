## Classic Image Processing (CIP)

CIP is a Java library focused in Image processing. It exposes Classic Image Processing functions in a convenient way for scripting, more specifically it is thought to be used in ImageJ script editor. It could be a good entry point to start scripting image analysis workflow and aim in general at easing prototyping. CIP tries to reach that goal by doing the following:

* Maintaining a carefully curated list of functions
  * Select functions already adopted by the community
  * Provide minimal set of functions to go from Image to measures
  * All functions works in 2D/3D (and nD whenever possible)
  * Aim to be on par with IJ1  speed-wise (and faster whenever possible)

* Easing functions discovery and learning
  * Consistent functions interfaces
  * Optional and named parameters
  * Strong documentation

* Making scripting easier to write and understand
  * Minimize the number of types to manipulate
  * Handling image types and conversion behind the scene
  * Minimizing library imports
  * Never modify function input

* Rely on existing infrastructure to allow CIP working hand in hand with imageJ ecosystem
  * Rely on ImgLib2 and ImageJ Op
  * Handling conversion to IJ1 and IJ2 image and regions
 
 
 
## Aimed functionalities
 
| Filters       | Segmentation     | Image wrangling | Image Math     | Misc          | 
| ---           | ---              | ---             | ---            | ---           |
| gaussian blur | manual threshold | copy            | add            | region        |
| erosion       | auto threshold   | duplicate       | sub            | IJ1 converters|
| dilation      | maxima           | slice           | multiply       | IJ2 converters|
| opening       | extended maxima  | project         | divide         | show          |
| closing       | multiscale maxima| concatenate     | logic operators| measures      |
| tophat        | binary watershed |                 | comparison     |
| distance map  | gray level watershed|              | sin, cos, ...  |
| median        | seeded watershed |                 | pow, sqrt      |
| gradient      | edge detection   |                 | floor, ceil, round|
| laplacian     | skeletonisation  |                 | log, exp       |
| hessian       |                  |                 | 
| Fillholes     |                  |                 |

 
## Installation

### to compile CIP source code
1. Clone [ImgAlgo Repository](https://github.com/benoitlo/ImgAlgo)
2. Import it as a Maven project in your IDE
3. Maven install the project
4. Clone [CIP repository](https://github.com/benoitlo/CIP)
5. import it as a Maven project in your IDE
6. compile as Maven project


### to use CIP in ImageJ
CIP and ImgAlgo jar will be soon available via an ImageJ update site soon 

1. Drop the ImgAlgo and CIP jar in your ImageJ plugins folder
2. Restart ImageJ
3. Open ImageJ script editor
4. Choose a language (For instance Python)
5. Add the annotation `#@CIP cip` at the beginning of the script
6. the variable cip is now available in the script and allows one to call any of CIP functionalities



