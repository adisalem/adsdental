# ADS Dental App

## How to Run (Minikube)

### Prerequisites
- Docker
- Minikube
- kubectl

### Steps
1) Start Minikube
```bash
minikube start --driver=docker
```

2) Build the app image inside Minikube
```bash
eval $(minikube -p minikube docker-env)
docker build -t adsdentalapp:1.0.0 .
```

3) Enable Ingress
```bash
minikube addons enable ingress
```

4) Deploy Postgres and the app
```bash
kubectl apply -f k8s/adsdental.yaml
```

5) Wait for pods to be ready
```bash
kubectl rollout status deployment/postgres
kubectl rollout status deployment/adsdental
```

6) Map the Ingress host to Minikube IP
```bash
minikube ip
```
Add the returned IP to your hosts file as:
```
<MINIKUBE_IP> adsdental.local
```

7) Open the app
```
http://adsdental.local
```

### Cleanup
```bash
kubectl delete -f k8s/adsdental.yaml
minikube delete
```
