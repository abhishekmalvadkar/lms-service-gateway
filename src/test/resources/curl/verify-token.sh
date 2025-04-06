curl -X POST "http://localhost:9093/api/auth/verify-token" \
     -H "Content-Type: application/json" \
     -d '{
           "token": "<YOUR_JWT_TOKEN>"
         }'