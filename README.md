# Nailshop

A nail polish shop made with Java and Spring Boot. You can create an account,
add products to a cart, and make Stripe test payments.

## Requirements

Docker with Docker Compose installed and running. Java, Maven, and MySQL
are installed inside Docker.

## Start

Run this in your computer's terminal, from the project folder:

```sh
docker compose up -d --build
```

The app starts automatically. If you use the private `compose.override.yaml`,
start Java with this command and leave the terminal open:

```sh
docker compose exec app mvn spring-boot:run
```

## Open

Open http://localhost:8080 in your browser.

## Demo accounts

The first start adds products, categories, demo accounts and sample orders.
You can log in with:

| Account | Email | Password |
| --- | --- | --- |
| Admin | admin@thenailshop.com | admin |
| Customer | customer@thenailshop.com | customer |

The data loads only into a new, empty database. Starting again keeps your data.

## Colima on Mac

If the app is running but localhost does not open (especially MacOs), make sure no other app uses
port 8080. Then run these in your computer's terminal:

```sh
colima stop
colima start --port-forwarder=ssh
docker compose up -d
```

This restarts the containers. If you use the private override, run the Maven
command above again.

In a second terminal, check the port:

```sh
docker compose port app 8080
curl --noproxy '*' -I --connect-timeout 3 http://127.0.0.1:8080/products
```

The port should show `0.0.0.0:8080`, and curl should return `HTTP 200`.
