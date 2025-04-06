curl -X POST "http://localhost:9093/api/lms/tags/fetch-tags" \
     -H "Authorization: Bearer <YOUR_JWT_TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{
           "pageNo": 1
         }'