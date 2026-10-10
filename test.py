import urllib.request
import json
import sys

API_KEY = "AIzaSyCOK9oChukVTrH02ZF7KVPeOEAlByGOCII"
# A simple Geocoding API test to verify if the key is active and billing is enabled
URL = f"https://maps.googleapis.com/maps/api/geocode/json?address=New+York&key={API_KEY}"

try:
    response = urllib.request.urlopen(URL)
    data = json.loads(response.read())

    status = data.get("status", "UNKNOWN")
    print(f"API Key Status: {status}")

    if status == "OK":
        print("✅ SUCCESS! The Google Maps API Key is VALID and ACTIVE.")
    elif status == "REQUEST_DENIED":
        print(f"❌ FAILED! The key was denied. Reason: {data.get('error_message')}")
    else:
        print(f"⚠️ UNEXPECTED STATUS: {status}. Message: {data.get('error_message')}")

except Exception as e:
    print(f"❌ FAILED to connect to Google API: {e}")
