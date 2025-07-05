FROM ubuntu:22.04

RUN uname -m

# Install OpenJDK manually or use an Azul JDK tar
RUN apt-get update && apt-get install -y zip unzip wget gnupg software-properties-common

# Add Azul repo and install OpenJDK 19
RUN wget -qO - https://repos.azul.com/azul-repo.key | apt-key add - && \
    apt-add-repository 'deb http://repos.azul.com/zulu/deb stable main' && \
    apt-get update && apt-get install -y zulu19-jdk

# Add Python 3.10 from deadsnakes
RUN add-apt-repository ppa:deadsnakes/ppa && \
    apt-get update && \
    apt-get install -y python3.10 python3.10-dev python3-pip python3.10-venv maven build-essential && \
    update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.10 1 && \
    pip3 install jep && \
    apt-get clean

WORKDIR /app
COPY . /app/

# Optional sanity check
RUN find / -name "libjep.so"

# Set env vars
#ENV JAVA_OPTS="-Djava.library.path=/usr/local/lib/python3.10/dist-packages/jep"
#ENV LD_LIBRARY_PATH="/usr/local/lib/python3.10/dist-packages/jep"

# Determine OS architecture type and create embedded python runtime
RUN OS_NAME=$(uname -s | tr '[:upper:]' '[:lower:]') && \
    ARCH=$(uname -m | tr '[:upper:]' '[:lower:]') && \
    if [ "$OS_NAME" = "darwin" ]; then \
        if [ "$ARCH" = "arm64" ]; then \
            OS_ARCH_TYPE="macos-arm64"; \
        else \
            OS_ARCH_TYPE="macos-x86_64"; \
        fi; \
    elif [ "$OS_NAME" = "linux" ]; then \
            case "$ARCH" in \
                x86_64)   OS_ARCH_TYPE="linux-x86_64" ;; \
                aarch64)  OS_ARCH_TYPE="linux-arm64"  ;; \
                arm64)    OS_ARCH_TYPE="linux-arm64"  ;; \
                *)        OS_ARCH_TYPE="linux-x86"    ;; \
            esac \
        else \
            OS_ARCH_TYPE="linux-x86_64"; \
    fi && \
    echo "OS_ARCH_TYPE=$OS_ARCH_TYPE" && \
    mkdir -p /tmp/embed-python/bin \
     && install -Dm755 /usr/bin/python3.10 /tmp/embed-python/bin/python3 \
     && cp -a /usr/bin/python3.10*         /tmp/embed-python/bin/ \
     # pip / pip3 launchers
     && cp -a /usr/bin/pip3*               /tmp/embed-python/bin/ \
     \
     && mkdir -p /tmp/embed-python/lib \
     && cp -r /usr/lib/python3.10          /tmp/embed-python/lib/ \
     \
     && mkdir -p /tmp/embed-python/include \
     && cp -r /usr/include/python3.10      /tmp/embed-python/include/ \
     \
     # ── copy distro-packaged pip, wheel, setuptools ────────────────
     && mkdir -p /tmp/embed-python/lib/python3.10/site-packages \
        && cp -a /usr/lib/python3/dist-packages/* /tmp/embed-python/lib/python3.10/site-packages/ \
     && pip3 install --no-cache-dir --upgrade \
            --prefix /tmp/embed-python pip setuptools wheel \
     && cd /tmp/embed-python \
     && zip -rq /tmp/python-3.10.zip . \
     && mkdir -p /app/generator/src/main/resources/python/${OS_ARCH_TYPE} \
     && mv /tmp/python-3.10.zip \
           /app/generator/src/main/resources/python/${OS_ARCH_TYPE}/


RUN cp /app/scripts/python/framework/analyzer.py /app/generator/src/main/resources/python/scripts/analyzer.py

# Build Java app
RUN mvn clean package -DskipTests

# Copy fat jar for runtime
RUN cp /app/example-wrapper-usage/target/example-wrapper-usage-1.0.0.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
