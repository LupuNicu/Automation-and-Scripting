FROM python:3.10-slim

# Instalăm cron și procps (pentru comanda ps)
RUN apt-get update && apt-get install -y cron procps && rm -rf /var/lib/apt/lists/*

# Setăm directorul de lucru
WORKDIR /app

# Copiem fișierele necesare
COPY currency_exchange_rate.py cronjob entrypoint.sh ./

# Instalăm dependențele Python
RUN pip install requests

# Permisiuni pentru scripturi
RUN chmod +x /app/entrypoint.sh

# Expunem logul cronului
RUN touch /var/log/cron.log

# Pornim containerul
ENTRYPOINT ["/app/entrypoint.sh"]
