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

### MinIO Credentials & UI (After docker compose starts)

- MinIO Web UI: http://localhost:9001
- Access Key: teletronics
- Secret Key: teletronics

### **Important:**
🔹 **Tags must be created before using them when uploading files.** If a user wants to assign tags to a file, they must first create those tags in the system.

### **Example API Requests using cURL**

#### **1. Upload a File**
```sh
curl -X POST "http://localhost:8080/files/" \
     -H "user_id: 123e4567-e89b-12d3-a456-426614174000" \
     -F "file=@/path/to/file.txt" \
     -F "is_public=true" \
     -F "tags=java,backend"
```

#### **2. Get File Upload Status**
```sh
curl -X GET "http://localhost:8080/files/status/{fileId}" 
```

#### **3. List Files (Paginated & Sorted)**
```sh
curl -X GET "http://localhost:8080/files/list?page=0&size=10&sortField=uploadDate&sortOrder=desc" \
     -H "user_id: 123e4567-e89b-12d3-a456-426614174000"
```

#### **4. Rename a File**
```sh
curl -X PUT "http://localhost:8080/files/{fileId}" \
     -H "user_id: 123e4567-e89b-12d3-a456-426614174000" \
     -d "newFilename=new_name.txt"
```

#### **5. Delete a File**
```sh
curl -X DELETE "http://localhost:8080/files/{fileId}" \
     -H "user_id: 123e4567-e89b-12d3-a456-426614174000"
```

#### **6. Create a Tag**
```sh
curl -X POST "http://localhost:8080/tags/" \
     -d "tagName=java"
```

#### **7. List Available Tags**
```sh
curl -X GET "http://localhost:8080/tags/list"
```

#### **8. Delete a Tag**
```sh
curl -X DELETE "http://localhost:8080/tags/delete?tagName=java"
```

---