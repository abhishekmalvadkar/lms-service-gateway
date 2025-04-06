curl -X POST "http://localhost:9093/api/lms/tags/create-tag" \
     -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{
           "tagName": "Java"
         }'