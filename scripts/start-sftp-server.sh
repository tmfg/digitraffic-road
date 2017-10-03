#!/bin/sh
BASEDIR=$(cd $(dirname $0); /bin/pwd)
METADATA_PATH=$BASEDIR/..

ip='127.0.0.1' #$(docker-machine ip $DOCKER_MACHINE_NAME)

# Set permissions suitable for sftp private host keys.
chmod og-rw $METADATA_PATH/src/test/resources/sftp/ssh_host_ed25519_key
chmod og-rw $METADATA_PATH/src/test/resources/sftp/ssh_host_rsa_key
chmod og-rw $METADATA_PATH/src/test/resources/sftp/client_id_rsa

docker run -d --rm \
    --name sftp_server \
    -v $METADATA_PATH/src/test/resources/sftp/client_id_rsa.pub:/home/digitraffic/.ssh/keys/id_rsa.pub:ro \
    -v $METADATA_PATH/src/test/resources/sftp/ssh_host_ed25519_key:/etc/ssh/ssh_host_ed25519_key \
    -v $METADATA_PATH/src/test/resources/sftp/ssh_host_ed25519_key.pub:/etc/ssh/ssh_host_ed25519_key.pub \
    -v $METADATA_PATH/src/test/resources/sftp/ssh_host_rsa_key:/etc/ssh/ssh_host_rsa_key \
    -v $METADATA_PATH/src/test/resources/sftp/ssh_host_rsa_key.pub:/etc/ssh/ssh_host_rsa_key.pub \
    -v $METADATA_PATH/test/weathercam-test.digitraffic.fi:/home/digitraffic/weathercam-test.digitraffic.fi \
    -p 2232:22 \
    atmoz/sftp \
    digitraffic:123:1000 && \
echo "sftp://${ip}:2232/ - username: digitraffic password: 123" && \
echo "sftp -P 2232 digitraffic@${ip}"
echo "sftp -i $METADATA_PATH/src/test/resources/sftp/client_id_rsa -P 2232 digitraffic@${ip}"
echo "client_id_rsa tiedostossa ei saa olla muilla lukuoikeuksia"
echo "client_id_rsa tiedoston passphrase on digitraffic"
echo "Digitraffic käyttäjän kansion weathercam-test.digitraffic.fi tiedostot näkyvät kansiossa $METADATA_PATH/test/weathercam-test.digitraffic.fi"
echo "Palvelin pysäytetään komennolla docker stop sftp_server"