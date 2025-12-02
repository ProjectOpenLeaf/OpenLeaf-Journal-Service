# OWASP ZAP Authentication Configuration for Journal Service
# This file helps ZAP authenticate with your Keycloak-protected endpoints

# ============================================================================
# AUTHENTICATION SCRIPT FOR ZAP
# ============================================================================
# To use authenticated scanning, you need to configure ZAP to obtain a JWT token
# from Keycloak and include it in all requests.

# Method 1: Manual Token Configuration (Simplest for CI/CD)
# ---------------------------------------------------------
# 1. Obtain a valid JWT token from Keycloak (use your test user)
# 2. Add it to ZAP context as a global header
# 3. Run scans with the authenticated context

# Method 2: ZAP Authentication Script (More complex, but automatic)
# -----------------------------------------------------------------
# This requires setting up a ZAP authentication script that:
# 1. Calls Keycloak token endpoint
# 2. Extracts JWT token
# 3. Injects it into all requests

# For CI/CD, we recommend Method 1 with a dedicated test user

# ============================================================================
# SAMPLE CURL COMMANDS TO TEST AUTHENTICATION
# ============================================================================

# Get token from Keycloak
get_token() {
    curl -X POST http://localhost:8080/realms/openleaf/protocol/openid-connect/token \
      -H "Content-Type: application/x-www-form-urlencoded" \
      -d "client_id=openleaf-client" \
      -d "client_secret=YOUR_CLIENT_SECRET" \
      -d "username=test-patient@example.com" \
      -d "password=testpassword" \
      -d "grant_type=password" | jq -r '.access_token'
}

# Test authenticated request
test_authenticated() {
    TOKEN=$(get_token)

    curl -X GET http://localhost:8083/api/journals \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json"
}

# ============================================================================
# ZAP CONTEXT CONFIGURATION (XML format)
# ============================================================================
# Save this as a .context file and import into ZAP

<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<configuration>
    <context>
        <name>Journal Service Context</name>
        <desc>Context for scanning Journal Service with Keycloak authentication</desc>
        <inscope>true</inscope>
        <incregexes>http://localhost:8083.*</incregexes>
        <tech>
            <include>Db.PostgreSQL</include>
            <include>Language.Java</include>
            <include>WS.REST</include>
            <include>OS.Linux</include>
        </tech>
        <authentication>
            <type>0</type><!-- Manual authentication -->
        </authentication>
        <users>
            <user>test-patient@example.com</user>
        </users>
        <forceduser>-1</forceduser>
    </context>
</configuration>

# ============================================================================
# ENVIRONMENT VARIABLES FOR ZAP SCANNING
# ============================================================================
export ZAP_AUTH_HEADER_NAME="Authorization"
export ZAP_AUTH_HEADER_VALUE="Bearer YOUR_JWT_TOKEN_HERE"

# ============================================================================
# ZAP CLI COMMAND WITH AUTHENTICATION
# ============================================================================
# Example command to run ZAP with authentication:

docker run --rm --network="host" \
    -v "$(pwd)/zap-reports:/zap/wrk/:rw" \
    -e ZAP_AUTH_HEADER="Authorization" \
    -e ZAP_AUTH_HEADER_VALUE="Bearer eyJhbGciOiJSUzI1NiIsInR5cCI..." \
    -t ghcr.io/zaproxy/zaproxy:stable \
    zap-baseline.py \
    -t http://localhost:8083 \
    -r zap-report.html \
    -z "-config replacer.full_list(0).enabled=true \
        -config replacer.full_list(0).description='auth' \
        -config replacer.full_list(0).matchtype=REQ_HEADER \
        -config replacer.full_list(0).matchstr=Authorization \
        -config replacer.full_list(0).replacement='Bearer YOUR_JWT_TOKEN'"

# ============================================================================
# NOTES
# ============================================================================
# - JWT tokens expire (typically 5-30 minutes)
# - For long scans, you may need to refresh the token
# - Consider creating a dedicated test user with limited permissions
# - Never commit real JWT tokens to version control
# - In production, use short-lived tokens and proper secret management