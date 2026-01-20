./kill.sh 2525
cd mb_plugin
./mb &
sleep 3
cd ..
curl -X POST http://localhost:2525/imposters \
  -H "Content-Type: application/json" \
  -d @open-ai.json

