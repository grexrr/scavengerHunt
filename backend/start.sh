#!/bin/bash

rm ./ngrok.log
rm src/main/resources/static/frontend_config.js

# run ngrok
ngrok http 8443 > ngrok.log&
sleep 10
wait

# extract URL from ngrok.log
LOCAL_HOST=$(grep -o 'https://[0-9a-z]*\.ngrok-free\.app' ngrok.log | head -n 1)

# Debug output
echo "Extracted LOCAL_HOST: $LOCAL_HOST"

echo "const LOCAL_HOST = '$LOCAL_HOST';" > src/main/resources/static/frontend_config.js

mvn spring-boot:run

