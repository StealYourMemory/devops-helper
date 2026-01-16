docker build -f Dockerfile -t uca-network-devops-helper:v1 . && \
docker run -d -p 48765:8765 \
		  -v /home/uca-network/devops-helper/log:/app/log \
           -v /home/uca-network/devops-helper/config:/app/config \
           -v /home/uca-network/devops-helper/db/devops_helper.sqlite:/app/devops_helper.sqlite \
           --name uca-network-devops-helper \
           uca-network-devops-helper:v1