# 1. Image nền chứa Java 21 & Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine

# 2. Thư mục làm việc trong container
WORKDIR /app

# 3. Copy pom.xml trước để tận dụng Docker cache layer
COPY pom.xml .

# 4. Copy mã nguồn vào container
COPY src ./src

# 5. Build file .jar (bỏ qua unit test)
RUN mvn clean package -DskipTests

# 6. Khai báo port 8080 cho Backend Spring Boot
EXPOSE 8080

# 7. Lệnh khởi chạy file .jar
CMD ["java", "-jar", "target/parking-lot-0.0.1-SNAPSHOT.jar"]
