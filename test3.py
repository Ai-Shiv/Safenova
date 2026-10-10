import requests

API_KEY = "AIzaSyCOK9oChukVTrH02ZF7KVPeOEAlByGOCII"

url = "https://maps.googleapis.com/maps/api/geocode/json"

params = {
    "address": "Varanasi, India",
    "key": API_KEY
}

response = requests.get(url, params=params, timeout=15)
data = response.json()

print("HTTP:", response.status_code)
print("Google status:", data.get("status"))
print("Error:", data.get("error_message"))

if data.get("status") == "OK":
    print("Coordinates:", data["results"][0]["geometry"]["location"])
