# Root - Cloud Storage Application

Welcome to the backend repository of **Root**, a cloud storage application built with **Java** and **Spring Boot**. The app is served over the DNS `cloud-storage.dev` and offers a rich set of features:

-  Document upload  
-  Document sharing  
-  Folder organization  
-  Tagging system  
-  Full account management  

---

## 🚀 Backend Tech Stack

Root is powered by a scalable and secure backend architecture:

###  Core
- **Java + Spring Boot**: Clean, modular REST APIs with rapid development.
- **Maven**: Build automation and dependency management.

###  Data
- **MySQL** via **AWS RDS**: Reliable relational storage for user and file metadata.
- **Redis**: Fast in-memory caching for sessions and metadata lookups.

###  Cloud Services
- **AWS S3**: Durable object storage for user files.
- **AWS EC2**: Hosts backend services with auto-scaling and load balancing.

---

##  CI/CD Pipeline

We use **GitHub Actions** for continuous integration and deployment:

- On every push or merged pull request to `main`, the app is built via Maven.
- After a successful build, the `target` folder is **rsynced** to the **AWS EC2** instance for deployment.

This setup ensures fast iteration and seamless delivery to production. The pipeline configuration is available in this repository.

---

##  How to Clone & Configure

To run the app locally or in your own environment, configure the following properties in `src/main/resources/application.yml`:

```yaml
datasource:
  url: your-database-url
  username: admin
  password: your-password

jpa:
  hibernate:
    ddl-auto: update
  show-sql: true
  properties:
    hibernate:
      dialect: org.hibernate.dialect.MySQL8Dialect

data:
  redis:
    host: localhost
    port: 6379
    database: 0

servlet:
  multipart:
    max-file-size: 10MB
    max-request-size: 10MB

security:
  oauth2:
    client:
      registration:
        google:
          client-id: your-google-id
          client-secret: your-google-secret
          scopes: openid, profile, email
          redirect-uri: "https://<YOUR DOMAIN>/api/oauth2/code/{registrationId}"
        github:
          client-id: your-github-id
          client-secret: your-github-secret
          scopes: openid, profile, email
          redirect-uri: "https://<YOUR DOMAIN>/api/oauth2/code/{registrationId}"

frontend:
  redirect:
    uri: http://your-frontend-domain.com/oauth2

bucket:
  name: your-main-bucket-name

profile:
  picture:
    bucket:
      name: your-profile-picture-bucket
      region: your-profile-picture-region

logging:
  level:
    org.springframework.security: DEBUG
    org.springframework.security.oauth2: DEBUG
    org.springframework.security.web.FilterChainProxy: DEBUG

server:
  address: 0.0.0.0
  port: 8080

jwt:
  secret: your_jwt_secret

email:
  username: your-email-username
  password: your-email-password
```

Be sure to have both the database and redis running. Also, if running on EC2, check if the instance has enough permissions for S3 and RDS management.
