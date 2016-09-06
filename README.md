# Mushy
A community-developed open-source v175 emulator for MapleStory.

## Installation
 * Clone this repository.
 * Download Global MapleStory and install it.
 * Follow most of this [installation guide](http://forum.ragezone.com/f428/maplestory-private-server-v83-741739/).
 * Download the necessary data files [found here](http://www.mediafire.com/download/i3z32pobguhr333/dat.rar), extract, and place inside the dat folder at the root folder.
 * Compile the source using an IDE of your choice. The jars go inside the bin folder, not the dist folder.
 * Double-click launch.bat to launch Mushy.
 
## Discord
Join the official Mushy Discord server to get a direct line of communication with the developers of Mushy, contributors, and other unaffiliated MapleStory developers, by [accepting this invitation](https://discord.gg/9nv3GPQ).

## Support
Should you run into any issues with the source, we **strongly** recommend that you ask for help on [RaGEZONE](http://forum.ragezone.com/f566/). Others might be having the same issue as you do so we'd rather have the solution to your problem broadcast to others.

## Authors
* Maxcloud
* SharpAceX (Alan)

## Contribute
If you would like to contribute to Mushy, please issue a pull request. Please follow the guidelines below if you do decide to submit a pull request. If you have any concerns with your pull requests, feel free to contact us over Discord.

##Pull Request Guide
###Solve a single problem per pull request.
If your pull request is addressing multiple things, you run a higher risk of something you submitted being denied. Since we cannot selectively pick out the parts of the pull request that we want, we have no choice but to deny the entire thing.

###Don't delete commented code for the sake of cleanliness.
While there's a great chance that the code actually is useless, you risk getting your pull request denied if you happen to delete commented code that actually is useful, so there's no point in risking it.

###Don't put your name on anything unless you did major work on that file.
By design, you will get credit for any code you submitted yourself. There is no need to manually add your name to the source. Your entire pull request will be denied if you do this.

###Do not change something completely irrelevant to the source's development.
Unless you have spoken with one of the developers about it beforehand, do not do things like change the rates, the channel number, etc. 

###Do not leave in debug/print statements unless it is extremely relevant to the issue at hand.
Debug on your end, but remove it before you submit the pull request please.

###Don't delete bytes unless you're absolutely sure you know what you're doing.
Packets rarely become shorter, so if you delete bytes that causes the entire packet to be shorter, we will automatically assume you don't know what you're doing unless you convince us otherwise.

###Do not collapse individual ints and shorts into a writeZeroBytes format.
Just don't do it. It's automatically wrong and almost always makes the packet worse.
