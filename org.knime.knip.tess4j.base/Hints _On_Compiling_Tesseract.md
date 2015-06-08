--------------------------------------
Hints on compiling tesseract
Author: Jonathan Hale
Last Update: 06.06.2015
--------------------------------------

Most parts of the compiling processs are
described in the tesseract wiki on the google
project page. But some minor issues may be
solved by the following hints.


All Platforms
-------

Build with mingw-w64 and msys on Windows, on OSX homebrew might help. On linux you need automake, aclocal and libtool.

Steps:
 0. Download leptonica source and build:
```
sh configure --without-zlib --without-libpng --without-jpeg --without-giflib --without-libtiff --without-libwebp --without-libopenjpeg
```

1. ./autogen.sh 

2. ./configure --without-tessdata-prefix --enable-shared

3. make -j<cores>
The parameter "-j" is not necessary, but speeds things up (will use <cores> cores [e.g. 4] ).

4. make install
Puts stuff where it belongs (usr/local/lib)

5. copy the installed stuff to the corresponding fragment.


Have fun! :)

