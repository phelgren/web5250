# web5250
#### An HTML based 5250 emulator (tn5250j servlet)
In 2002 I was in the process of designing an HTML based menuing system that would allow both HTML and 5250 applications to be launched. The HTML part was easy, but launching a 5250 application from an HTML menu was a bit more of a challenge. Not that there wasn't a great, open source, 5250 emulator available (there is, it's called tn5250j). It was just that in order to accommodate launching tn5250j from an HTML page, I needed to use an applet. Applets require a JVM and JVM's on the client can be an administrative hassle. So, with all that in mind, I started looking for an HTML based 5250 (or even telnet) open source client. As it turned out, I didn't have to go very far. The tn5250j project already had most of the infrastucture fleshed out and with some help (quite a bit, from Kenneth Pouncey, tn5250 project lead).

#### Version 2.0

After a couple of decades, I assumed the idea and the need for a HTML based 5250 emulator was dead.  I got the occational ping about the project but then I got a few back and forth emails from the midrange.com list that I subscribe to and I blew the dust off the code and updated it (a bit).  I will continue to say that I am not happy with it.  There are still some function key and cursor issues. I don't fully understand the data flows (sometimes it seems field values are cached).  Field references are sequential (FLD1, FLD2 and so on)  so I think those values sometimes make their way back to the UI after an error.  So, it is very much a work in progress.

##### NOTE: The keyboard artifacts are leftovers from an attempt I was going to make to be able to map keys 5250 style.  I never really got it going....
