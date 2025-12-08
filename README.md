# 🚀 Template Monorepo

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![TypeScript](https://img.shields.io/badge/Code-TypeScript-294E80.svg)](https://www.typescriptlang.org/)
[![npm](https://img.shields.io/badge/Package_Manager-npm-CB3837.svg)](https://www.npmjs.com/)

This repository is structured as a **Monorepo**. It separates the application into distinct, independently deployable packages.

## 🛠️ Tech Stack Overview

| Area | Package | Technology | Description |
| :--- | :--- | :--- | :--- |
| **Backend** | `backend` | **Spring Boot**, Java | REST API built on Java, providing core application logic and data access. |
| **Frontend** | `frontend` | **React**, **Vite**, TypeScript | Single Page Application (SPA) for the user interface. |

## 📦 Monorepo Structure
```
.
├── backend/            # Spring Boot API package
├── frontend/           # React/Vite UI package
└── package.json        # Root configuration
```

## ⚙️ Getting Started

### Prerequisites

* Node.js (LTS version)
* npm package manager
* Java JDK

### Installation

#### Install all dependencies:
Run this command from the `frontend` directory.
```
npm install
```

## 🚀 Available Scripts

All major operations are executed from their respective directories.

### Development

| Command | Target Directory | Description |
| :--- | :--- | :--- |
| `./mvnw spring-boot:run` | `backend` | Starts the Spring Boot backend. |
| `npm run dev` | `frontend` | Starts the React development server via Vite. |

### Building and Linting

| Command | Target Directory | Description |
| :--- | :--- | :--- |
| `./mvnw clean package` | `backend` | Builds the backend application. |
| `npm run build` | `frontend` | Builds the frontend application. |
| `npm run lint` | `frontend` | Runs linter on the frontend. |
