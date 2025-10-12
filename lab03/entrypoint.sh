#!/bin/sh

create_log_file() {
    echo "Creating log file..."
    touch /var/log/cron.log
    chmod 666 /var/log/cron.log
    echo "Log file created at /var/log/cron.log"
}

setup_cron() {
    echo "Setting up cron jobs..."
    crontab /app/cronjob
    echo "Cron jobs installed:"
    crontab -l
}

monitor_logs() {
    echo "=== Monitoring cron logs ==="
    tail -f /var/log/cron.log &
}

run_cron() {
    echo "=== Starting cron daemon ==="
    exec cron -f
}

env > /etc/environment
create_log_file
setup_cron
monitor_logs
run_cron
