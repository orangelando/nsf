# NSF renderer in Java

Java project to render the music within a Nintendo Sound Format (.nsf) file
to a wav file. This is still definitely a WIP. The 6502 core is not cycle accurate.
I wanted to see if it is possible to do a higher level interpretation of the CPU
and still get accurate results. So far it seems to be working.

## Usage

### Prerequisites

Maven is required so that the fat jar can be built:

```sh
$ mvn package
... bunch of stuff ...
[INFO] BUILD SUCCESS
[INFO] --------------------------------------
[INFO] Total time: 11.201 s
[INFO] Finished at: 2019-02-04T20:50:13-05:00
[INFO] --------------------------------------

```

### Viewing NSF Header Examples

```sh
$ ./lando-nsf-info ~/Desktop/nsf/metroid.nsf 
Version Number    : 1
Total Songs       : 12
Starting Song     : 1

Song Name         : Metroid
Artist Name       : Hirokazu "Hip" Tanaka
Copyright Holder  : 1986 Nintendo

NTSC/Pal Mode     : NTSC
NTSC Speed        : 16666
PAL Speed         : 0

Bankswitch Vals   : [5, 5, 5, 5, 5, 5, 5, 5]
Extra Sound Chips : []

Load Data Addr    : $8000
Init Data Addr    : $a000
Play Data Addr    : $b3b4
```

Add the "-json" argument to the results in JSON.

### Extracting a Single Track to a WAV File

NSF files that are extracted from NES ROMs are under copying and cannot be 
distributed so please provide your own NSF files.

```sh
$ ./lando-nsf2wav -nsfFile ~/Desktop/nsf/super-mario-bros-2.nsf -trackNum 2 -outFile ~/Desktop/out.wav

```

### Extracting All Tracks at Once

NSF files that are extracted from NES ROMs are under copying and cannot be 
distributed so please provide your own NSF files.

```sh
$ ./lando-nsf2wav -nsfFile ~/Desktop/nsf/super-mario-bros-2.nsf -allTracks -outFile ~/Desktop/out.wav

```

The full list of options are:
 * **-nsfFile path** Path to .nsf file
 * **-outFile path** Path to output file
 * **-trackNum N** Track num to extract
 * **-allTracks** Extract all tracks. Appends '-00', '-01' etc to the output file.
 * **-disableChannels 12tnd** Disable one or more APU channels. 1=pulse1, 2=pulse2, t=triangle, n=noise, d=dmc
 * **-maxPlaysSecs 30** Many tracks loop forever. This is the maximum play time before the track fades out. 30 is the default.
 * **-maxSilenceSecs 3** The tracks is stopped playing if more than this many seconds of silence is detected. 3 is the default.
 * **-disableBandPass** Disables the highpass filter at 440hz and a lowpass filter at 14khz in the wav output.
 * **-splitChannels** Output each channel into a separate file.
 * **-outFmt wav|system_raw** The system-raw option outputs to 32-bit, little endian float samples per 1.79MHz clock tick. Useful for debugging. Default is wav which outputs a standard wav file with mono 16bit PCM samples at 44.1kHz.

