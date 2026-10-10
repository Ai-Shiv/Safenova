import requests
import os

API_KEY = "AIzaSyCOK9oChukVTrH02ZF7KVPeOEAlByGOCII"

url = "https://maps.googleapis.com/maps/api/geocode/json"

params = {
    "address": "Galgotias University, Greater Noida",
    "key": API_KEY
}

response = requests.get(url, params=params)
data = response.json()

print("Status:", data.get("status"))
print(data)
