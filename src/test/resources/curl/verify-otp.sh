curl -i -X POST "http://localhost:9093/api/auth/verify-otp" \
     -H "Content-Type: application/json" \
     -H "device: web" \
     -d '{
           "email": "<YOUR_EMAIL>",
           "otp": "<YOUR_OTP>"
         }'