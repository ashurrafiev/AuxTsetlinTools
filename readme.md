# Tsetlin Machine Toolkit

## Logger Generator

**Logger generator** automatically creates `TsetlinLoggerDefs.h` and `TsetlinLogger.h` files from an XML description.
The generated files can save TM state and other data in CSV format for visualization. The API is (supposedly) generic enough
to be compatible with any C implementation of the TM; see logger XML [documentation](loggerxml.md).

Package: [ncl.tsetlin.tools.genlogger](src/ncl/tsetlin/tools/genlogger)  
Main class: [GenLogger](src/ncl/tsetlin/tools/genlogger/GenLogger.java)  
Usage:
```
java -cp bin ncl.tsetlin.tools.genlogger.GenLogger logger.xml
```


## Packed-Bits Format

Packed-Bits Format **pkbits** is a compact binarized input data format used by [**ClassParallelTM**](https://github.com/ashurrafiev/ClassParallelTM).

Package: [ncl.tsetlin.tools.pkbits](src/ncl/tsetlin/tools/pkbits)

* `MnistData` is a helper class that can read the original MNIST database.
* `MnistToPng` renders glyph previews into a large PNG image.
* `ClassFilter` separates MNIST data into independent "class-streams" in **pkbits** format where `false` data items are chosen randomly.
* `PkBitsOutputStream` is a helper class for writing **pkbits**; see `MnistData.writeAsPkBits()` for the use example.

## TA State Spectrogram

Package: [ncl.tsetlin.tools.spectrum](src/ncl/tsetlin/tools/spectrum)


## Clause Similarity Chart

Package: [ncl.tsetlin.tools.clauses](src/ncl/tsetlin/tools/clauses)


