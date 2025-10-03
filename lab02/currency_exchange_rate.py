import os
import sys
import json
import requests
from datetime import datetime

def main():
    API_KEY = "NICU_LUPU_API_KEY"

    if len(sys.argv) != 4:
        print("Usage: python currency_exchange_rate.py <from_currency> <to_currency> <date: YYYY-MM-DD>")
        sys.exit(1)

    from_currency = sys.argv[1].upper()
    to_currency = sys.argv[2].upper()
    date = sys.argv[3]

    # validăm formatul datei
    try:
        datetime.strptime(date, "%Y-%m-%d")
    except ValueError:
        log_error(f"Invalid date format: {date}. Expected YYYY-MM-DD.")
        sys.exit(1)

    # endpoint API
    url = "http://localhost:8888/"
    params = {"from": from_currency, "to": to_currency, "date": date}
    data = {"key": API_KEY}

    try:
        response = requests.post(url, params=params, data=data, timeout=10)
        response.raise_for_status()
        result = response.json()

        if result.get("error"):
            log_error(f"API error: {result['error']}")
            sys.exit(1)

        # salvăm datele în fișier JSON
        os.makedirs("data", exist_ok=True)
        filename = f"data/{from_currency}_{to_currency}_{date}.json"
        with open(filename, "w", encoding="utf-8") as f:
            json.dump(result, f, indent=4, ensure_ascii=False)

        print(f"✅ Data saved to {filename}")

    except requests.exceptions.RequestException as e:
        log_error(f"Request failed: {e}")
        sys.exit(1)
    except ValueError as e:
        log_error(f"Invalid JSON response: {e}")
        sys.exit(1)


def log_error(message: str):
    print(f"❌ Error: {message}")
    with open("error.log", "a", encoding="utf-8") as f:
        f.write(f"{datetime.now().isoformat()} - {message}\n")


if __name__ == "__main__":
    main()
