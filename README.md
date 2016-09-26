# ClearCL #

OpenCL Facade Object Oriented API. 

* Planned features:

1- Support all Java OpenCL bindings (JOCL, JavaCL, Jogamp, LWJGL)
2- Full support of OpenCL 1.2
3- Support for offheap memory (> 2G) via CoreMem (http://github.com/ClearControl/CoreMem)
4- Basic set of OpenCL kernels for image processing: denoising, deconvolution, image quality, correlation, projection...
5- Scatter-gather for processing images and buffers that don't fit in GPU mem.
6- Upload-and-scaledown functionality to load and scale down images into GPU memory
7- Live-coding infrastructire to be able to edit kernel code and immediately see the result.

* Current state:

Full Object Oriented API supporting all OpenCL 1.2 features except events. 
Currently the only supported backend is JOCL (http://www.jocl.org/).

Changing backend requires just one code line change.


### How to build project with Gradle

* Get Gradle [here](http://www.gradle.org/)

* Go to the project folder root and run:

     gradle build cleanEclipse eclipse
     
### How to get started with ClearCL

* add ClearCL as a dependency to your project:

With Gradle:
~~~~
     compile 'net.clearvolume:clearcl:0.1.0'
~~~~

~~~~
repositories {
    maven {
        url  "http://dl.bintray.com/clearvolume/ClearVolume" 
    }
}
~~~~

With Maven:
~~~~
<dependency>
  <groupId>net.clearvolume</groupId>
  <artifactId>clearcl</artifactId>
  <version>0.1.0</version>
  <type>pom</type>
</dependency>
~~~~

~~~~
<repository>
     <snapshots>
         <enabled>false</enabled>
     </snapshots>
     <id>bintray-clearvolume-ClearVolume</id>
     <name>bintray</name>
     <url>http://dl.bintray.com/clearvolume/ClearVolume</url>
 </repository>
~~~~

* Just check the test (here)[https://github.com/ClearVolume/ClearCL/blob/master/src/java/clearcl/test/ClearCLTests.java] to learn how to use ClearCL. More tests are coming...

### Contributors ###

* Loic Royer (royer -at- mpi-cbg -point- de)
