# **Project Overview**

## **Introduction**

The **STORAGE** application is a REST API designed to provide users with a simple and efficient way to store, manage, and share files. The application allows users to upload files with metadata, list stored files with filtering and sorting capabilities, rename and delete files, and generate unique download links for both **public** and **private** access.

This project is implemented using **Spring Boot**, **MongoDB**, and **AWS S3 (or MinIO)** for file storage. It follows modern development best practices, including **unit testing**, and **containerization with Docker**.

## **Key Features**

### **Functional Requirements**

✔️ **File Upload**

- Users can upload files with a **filename**, **visibility setting** (PUBLIC / PRIVATE), and a set of **tags** (up to 5).
- File size is **not limited**, supporting uploads from a few KB to hundreds of GB.
- The application **prevents duplicate uploads** based on **content hash** or **filename**.
- **Asynchronous file upload process** ensures better performance and scalability.

✔️ **File Management**

- Users can **rename** uploaded files.
- Users can **delete** only their own files.

✔️ **File Listing & Filtering**

- Users can retrieve a **paginated list of files**, filtering by **tag** and sorting by:
    - **Filename**
    - **Upload Date**
    - **Tag**
    - **Content Type**
    - **File Size**
- Users can view **all PUBLIC files** or **only their own files**.

✔️ **File Identification & Download**

- File **content type is detected automatically** after upload.
- Each file receives a **unique, non-guessable download link**.
- Both **PRIVATE and PUBLIC files** can be downloaded via this link.

### **Non-Functional Requirements**

✔️ **Backend Framework:** Implemented with **Spring Boot** for rapid development and scalability.\
✔️ **Database:** Uses **MongoDB** for metadata storage.\
✔️ **Storage:** Uses **AWS S3 / MinIO** for efficient and scalable object storage.\
✔️ **Testing:** Includes **unit tests and integration tests** for controllers and services.\
✔️ **Containerization:** The application is built using **Gradle** and shipped as a **Docker image**.\
✔️ **CI/CD Integration:** Hosted on **GitHub**, with **automated build pipelines**.\
✔️ **Documentation:** Includes a **README** detailing API usage and deployment instructions.

## **Technology Stack**

| Component            | Technology Used               |
| -------------------- | ----------------------------- |
| **Backend**          | Java 17, Spring Boot 3        |
| **Database**         | MongoDB                       |
| **Storage**          | AWS S3 / MinIO                |
| **Testing**          | JUnit 5, Mockito, Spring Test |
| **Build Tool**       | Gradle                        |
| **Containerization** | Docker                        |
| **CI/CD**            | GitHub Actions                |

---

## **How to Use**

### **Running the Application with Docker Compose**

1. Ensure you have **Docker** installed on your system.
2. Navigate to the `dev` folder in the project directory.
3. Run the following command to build the application, run tests, and start the services:
   ```sh
   docker compose up
   ```

This command will:

- Build the application using **Gradle**.
- Run unit and integration tests.
- Start the application along with **MongoDB** and **MinIO** storage.

4. Once the application is running, you can access the API using tools like **Postman** or **cURL**.

