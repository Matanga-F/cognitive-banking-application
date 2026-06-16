# ============================
# Generate JWT with OpenSSL (PowerShell)
# ============================

# CONFIGURATION - Use ONLY ONE of these:
$secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
# $secret = "your-secret-key"  # <- Comment this out if using above

# Token expiration (seconds from now)
$expirySeconds = 1800  # 30 minutes

# Header and Payload
$header = '{"alg":"HS256","typ":"JWT"}'
$iat = [int][double]::Parse((Get-Date -UFormat %s))
$exp = $iat + $expirySeconds
$payload = '{"sub":"admin","iat":' + $iat + ',"exp":' + $exp + ',"roles":["ADMIN"]}'

function Encode-Base64Url($input) {
    $bytes = [System.Text.Encoding]::UTF8.GetBytes($input)
    $encoded = [System.Convert]::ToBase64String($bytes)
    $encoded = $encoded.TrimEnd('=').Replace('+','-').Replace('/','_')
    return $encoded
}

$headerEncoded = Encode-Base64Url $header
$payloadEncoded = Encode-Base64Url $payload
$signingInput = "$headerEncoded.$payloadEncoded"

# Sign with HMAC SHA256 using OpenSSL
$signature = echo -n $signingInput | openssl dgst -sha256 -hmac $secret -binary | openssl base64 -A
$signature = $signature.TrimEnd('=').Replace('+','-').Replace('/','_')

# Final JWT
$jwt = "$signingInput.$signature"

# Save to file
$newTokenFile = "new_token.txt"
Set-Content -Path $newTokenFile -Value $jwt

Write-Host "`n========================================"
Write-Host "GENERATED JWT TOKEN"
Write-Host "========================================"
Write-Host $jwt
Write-Host ""
Write-Host "Token saved to: $newTokenFile"
Write-Host ""
Write-Host "Claims:"
Write-Host "  Subject: admin"
Write-Host "  Issued At: $iat"
Write-Host "  Expires: $exp ($expirySeconds seconds from now)"
Write-Host "  Roles: ADMIN"
Write-Host "========================================"
