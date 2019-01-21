# ...Hello
(30-3-2018) This is the second JavaFx Application I developed. 

The program's backend uses java's socket programming to allow two devices to communicate with each other.
The user is given the choice to host a local server or connect to another device through the ipv4 address.

The frontend is developed using JavaFx which includes many design options like emotes (both images and gifs) and different sounds that play on certain events.


Possible improvements:

1. The messages that contain emote names are converted to their corresponding res file based on a concatenating algorithm I developed. There is probably a much more efficient method of achieving the same result through REGEX.

2. Use multi-threading/asynchronous to avoid I/O blocking - When the client connects to a server, the program freezes until it connects.
