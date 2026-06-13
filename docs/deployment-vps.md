# Despliegue en VPS — Marketplace Link

Guía para desplegar **backend + base de datos + frontend** en un VPS Linux con
Docker, sirviendo todo detrás de un Nginx reverse proxy con HTTPS.

La aplicación se compone de dos repositorios con **composes separados**:

- `Marketplace_Link` (backend) → crea la base de datos y la red Docker `back_mplink_net`.
- `Marketplace-Link-Front` (frontend) → se conecta a esa red como red externa.

> **Orden de arranque:** primero el backend (crea la red), luego el frontend.

---

## 1. Requisitos del VPS

- Ubuntu/Debian (u otra distro con systemd), 2 vCPU / 2–4 GB RAM recomendado.
- Un dominio apuntando (registro A) a la IP del VPS, p. ej. `midominio.com`.
- Puertos abiertos: `80` y `443`.

## 2. Instalar Docker y Docker Compose

```bash
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER   # reinicia la sesión SSH después
docker compose version          # verifica el plugin compose v2
```

## 3. Clonar los repositorios

```bash
mkdir -p ~/apps && cd ~/apps
git clone <url-del-backend>  Marketplace_Link
git clone <url-del-frontend> Marketplace-Link-Front
```

## 4. Configurar variables de entorno

### Backend

```bash
cd ~/apps/Marketplace_Link
cp .env.example .env
nano .env
```

Define como mínimo:
- `DB_PASSWORD` (una contraseña fuerte)
- `JWT_SECRET` → genera con `openssl rand -base64 48`
- `MAIL_*` (credenciales SMTP reales)
- `FRONTEND_URL=https://midominio.com`

### Frontend

```bash
cd ~/apps/Marketplace-Link-Front
cp .env.example .env
nano .env
```

Define:
- `VITE_API_URL=https://midominio.com/api`
- `VITE_FRONTEND_URL=https://midominio.com`
- `FRONT_PORT=5174`  (puerto interno; el acceso público va por Nginx)

## 5. Levantar la aplicación

```bash
# 1) Backend + base de datos (crea la red back_mplink_net)
cd ~/apps/Marketplace_Link
docker compose up -d --build

# 2) Frontend (usa la red externa creada por el backend)
cd ~/apps/Marketplace-Link-Front
docker compose up -d --build
```

Comprobar:

```bash
docker compose ps
curl -f http://localhost:8080/actuator/health   # backend OK
curl -I http://localhost:5174                    # frontend OK
```

## 6. Nginx reverse proxy (un solo dominio)

Instala Nginx en el host (fuera de Docker) para enrutar:
- `/`     → frontend (contenedor en `:5174`)
- `/api/` → backend  (contenedor en `:8080`)

```bash
sudo apt update && sudo apt install -y nginx
sudo nano /etc/nginx/sites-available/marketplace
```

```nginx
server {
    listen 80;
    server_name midominio.com www.midominio.com;

    # Frontend (SPA)
    location / {
        proxy_pass http://127.0.0.1:5174;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Backend API — se elimina el prefijo /api antes de pasar al backend
    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 50M;
    }
}
```

```bash
sudo ln -s /etc/nginx/sites-available/marketplace /etc/nginx/sites-enabled/
sudo nginx -t && sudo systemctl reload nginx
```

> Con `VITE_API_URL=https://midominio.com/api`, el frontend llamará a
> `https://midominio.com/api/...` y Nginx lo reenviará al backend en `:8080`.
> Asegúrate de que `FRONTEND_URL` (backend) incluya `https://midominio.com`
> para que CORS permita las peticiones.

## 7. HTTPS con Certbot (Let's Encrypt)

```bash
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d midominio.com -d www.midominio.com
```

Certbot edita la config de Nginx para servir en `443` y renueva el certificado
automáticamente (`systemctl status certbot.timer`).

## 8. Operación

### Logs

```bash
# Backend / DB
cd ~/apps/Marketplace_Link && docker compose logs -f
# Frontend
cd ~/apps/Marketplace-Link-Front && docker compose logs -f
# Nginx host
sudo tail -f /var/log/nginx/error.log
```

### Reiniciar / detener

```bash
docker compose restart            # reiniciar servicios del compose actual
docker compose down               # detener (conserva datos)
docker compose down -v            # detener y BORRAR datos persistentes (¡cuidado!)
```

### Backup de la base de datos

```bash
docker exec mplink_marketplace_db pg_dump -U postgres marketplace_db \
  > backup_$(date +%F).sql
```

Restauración:

```bash
cat backup_2026-06-13.sql | \
  docker exec -i mplink_marketplace_db psql -U postgres -d marketplace_db
```

### Actualizar tras nuevos commits

```bash
# Backend
cd ~/apps/Marketplace_Link
git pull
docker compose up -d --build

# Frontend
cd ~/apps/Marketplace-Link-Front
git pull
docker compose up -d --build
```

## 9. Checklist de seguridad

- [ ] `JWT_SECRET`, `DB_PASSWORD`, `MODERATOR_DEFAULT_PASSWORD` propios y fuertes.
- [ ] El puerto de PostgreSQL **no** está publicado al exterior (solo red Docker).
      En producción puedes quitar el mapeo `ports:` de `mplink_postgres`.
- [ ] Datos semilla de `docker/init.sql` revisados (no dejar `admin/admin123`).
- [ ] Firewall: solo `80` y `443` abiertos públicamente (`ufw`).
- [ ] HTTPS activo y redirección de HTTP a HTTPS (Certbot lo configura).
- [ ] Valorar `springdoc.swagger-ui.enabled=false` en producción.
