# WbPlumbing

Wishbone plumbing written in [Chisel](https://www.chisel-lang.org/). The aim of this project is mainly to
generate Wishbone intercon as described in [specification](https://github.com/fossi-foundation/wishbone).

# Installation and usages

## Install

WbPlumbing is a scala package that can be published localy. Then to install it we can simply clone the project and publish it localy 
```shell
$ git clone https://github.com/Martoni/WbPlumbing.git
$ cd WbPlumbing
$ sbt "publishLocal"
```

To use it under your project, add this line in your `build.sbt` file :
```scala
libraryDependencies ++= Seq("org.armadeus" %% "wbplumbing" % "6.2.0")
```

And import package in your chisel code :
```scala
import wbplumbing
```

## Test

[Verilator](https://verilator.org) version `5.012` should be used to avoid
`C++14` dependency error.

Then use `sbt` to launch tests:

```Shell
$ sbt test
[info] welcome to sbt 1.4.9 (Private Build Java 16.0.1)
[info] loading project definition from /opt/chiselmod/WbPlumbing/project
[info] loading settings for project root from build.sbt ...
[info] set current project to wbplumbing (in build file:/opt/chiselmod/WbPlumbing/)
@00000000: Ksz1
@00000008: Ksz2
[info] WbInterconPTSpec:
[info] - WbInterconPT should read and write wishbone value on one slave
[info] WbInterconOneMasterSpec:
[info] - A WbInterconOneMaster should read and write wishbone value on two slaves
[info] Run completed in 3 seconds, 615 milliseconds.
[info] Total number of tests run: 2
[info] Suites: completed 2, aborted 0
[info] Tests: succeeded 2, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[success] Total time: 4 s, completed 7 mars 2024, 17:21:19
```

# Modules description

## WbInterconPT

This is just a "passthrought" module, give one WbMaster bundle and one WbSlave
bundle in parameters and get the intercon module for your plumbing.

To use it simply give a WbMaster bundle and WbSlave bundle as parameter like following example :
```scala
// Imports
import wbplumbing.WbInterconPT
import wbplumbing.{WbMaster, WbSlave}

//...

// module instantiation
val spi2Wb = Module(new Spi2Wb(dwidth, awidth))
val wbMdio = Module(new MdioWb(mainFreq, targetFreq))
val wbIntercon = Module(new WbInterconPT(spi2Wb.io.wbm, wbMdio.io.wbs))
// wishbone Interconnexion
spi2Wb.io.wbm <> wbIntercon.io.wbm
wbIntercon.io.wbs <> wbMdio.io.wbs

// ...
```

## WbInterconOneMaster

Make the address decoding for several slaves with the same data size. And with
only one master.

To use it the master bundle must be given 'as it' in parameter, but slave should be given as a Seq() parameters like following example :
```scala
  // module instantiation
  val mspi2Wb = Module(new Spi2Wb(dwidth, 7))
  val wbMdio1 = Module(new MdioWb(mainFreq, targetFreq))
  val wbMdio2 = Module(new MdioWb(mainFreq, targetFreq))

  val wbIntercon = Module(new WbInterconOneMaster(mspi2Wb.io.wbm,
                            Seq(wbMdio1.io.wbs, wbMdio2.io.wbs)))
  // wishbone Interconnexion
  mspi2Wb.io.wbm <> wbIntercon.io.wbm
  wbIntercon.io.wbs(0) <> wbMdio1.io.wbs
  wbIntercon.io.wbs(1) <> wbMdio2.io.wbs
```

# Projects using WbPlumbing

* [MDIO](https://github.com/Martoni/MDIO): Generate ethernet phy MDIO protocol frame
* [Spi2Wb](https://github.com/Martoni/spi2wb): Drive a Wishbone master bus with SPI protocol.
* [wbGPIO](https://github.com/Martoni/wbGPIO): General Purpose Input Output
