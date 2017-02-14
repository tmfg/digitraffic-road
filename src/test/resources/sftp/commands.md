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

AbstractSftpTest generates server private/public keys and updates client_known_host to include server public key  
* target/test-classes/sftp/server_id_rsa
* target/test-classes/sftp/server_id_rsa.pub
* target/test-classes/sftp/client_known_host

## Setup keys

Add **client\_id\_rsa.pub** contents to server_authorized_keys

	$ cat client_id_rsa.pub > server_authorized_keys
	
Copy generated keys after first run of *AbstractSftpTest*

	$ cp target/test-classes/sftp/server_id_rsa src/test/resources/sftp/
	$ cp target/test-classes/sftp/server_id_rsa.pub src/test/resources/sftp/
	$ cp target/test-classes/sftp/client_known_hosts src/test/resources/sftp/