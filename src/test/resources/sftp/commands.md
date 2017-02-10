## Generate client and server keys
### Client key

~~~bash
$ ssh-keygen -t rsa -b 4096 -C "digitraffic@solita.fi"
Generating public/private rsa key pair.
Enter file in which to save the key (/Users/jouniso/.ssh/id_rsa): /Users/jouniso/tyo/digitraffic/idea/digitraffic-metadata/src/test/resources/sftp/client_id_rsa
Enter passphrase (empty for no passphrase): [digitraffic]
Enter same passphrase again:
Your identification has been saved in /Users/jouniso/tyo/digitraffic/idea/digitraffic-metadata/src/test/resources/sftp/client_id_rsa.
Your public key has been saved in /Users/jouniso/tyo/digitraffic/idea/digitraffic-metadata/src/test/resources/sftp/client_id_rsa.pub.
The key fingerprint is:
SHA256:+2Qydfh9+EnBDi5vIAxNjSsSpbYXktm1ETzbNQvRSNQ digitraffic@solita.fi
The key's randomart image is:
+---[RSA 4096]----+
|      ...+*+=    |
|     .= .=o+ E   |
|     *.oo.= o o  |
|    ..oo.+ o ..  |
|     ...S o .. o |
|      .  = +..o..|
|        + +.o.oo.|
|         *  o..o.|
|          . .. ..|
+----[SHA256]-----+
~~~

### Server key

~~~bash
$ ssh-keygen -t rsa -b 4096 -C "digitraffic@solita.fi"
Generating public/private rsa key pair.
Enter file in which to save the key (/Users/jouniso/.ssh/id_rsa): /Users/jouniso/tyo/digitraffic/idea/digitraffic-metadata/src/test/resources/sftp/server_id_rsa
Enter passphrase (empty for no passphrase): [empty]
Enter same passphrase again:
Your identification has been saved in /Users/jouniso/tyo/digitraffic/idea/digitraffic-metadata/src/test/resources/sftp/server_id_rsa.
Your public key has been saved in /Users/jouniso/tyo/digitraffic/idea/digitraffic-metadata/src/test/resources/sftp/server_id_rsa.pub.
The key fingerprint is:
SHA256:/qa9zdgueReMZRxPUP4OWM6SSHAe8adi7320bkQe5Vw digitraffic@solita.fi
The key's randomart image is:
+---[RSA 4096]----+
|        . +.  .o.|
|         + o  ..E|
|          o ..o*+|
|         . . O+o=|
|        S + ==* o|
|       . . o..o=.|
|        .  .. .oo|
|         o+*...o.|
|        .o==*.+o |
+----[SHA256]-----+
~~~

## Setup keys

Add **client\_id\_rsa.pub** contents to server\_authorized\_keys

	$ cat client_id_rsa.pub > server_authorized_keys
	
Add ** server\_id\_rsa.pub** contents to **client\_known\_hosts**

	$ echo -n "[localhost]:2232 " > client_known_hosts
	$ cat server_id_rsa.pub >> client_known_hosts