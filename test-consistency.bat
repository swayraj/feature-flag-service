@echo off
echo Testing consistency for user-123...
echo.
echo Request 1:
curl -s "http://localhost:8080/api/evaluate/new_checkout?userId=user-123"
echo.
echo.
echo Request 2:
curl -s "http://localhost:8080/api/evaluate/new_checkout?userId=user-123"
echo.
echo.
echo Request 3:
curl -s "http://localhost:8080/api/evaluate/new_checkout?userId=user-123"
echo.
echo.
echo All three should be identical!