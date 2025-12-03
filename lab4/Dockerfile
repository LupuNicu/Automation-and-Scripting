FROM jenkins/ssh-agent

# Install PHP-CLI, Git, curl, unzip and PHP extensions
RUN apt-get update && apt-get install -y \
    php-cli \
    php-xml \
    php-curl \
    php-mbstring \
    php-zip \
    git \
    curl \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Install Composer
RUN curl -sS https://getcomposer.org/installer | php -- --install-dir=/usr/local/bin --filename=composer \
    && chmod +x /usr/local/bin/composer

# Verify installations
RUN php --version && composer --version && git --version && php -m | grep -E "(dom|curl|xml)"

