# 🚀 Template Monorepo

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)
[![TypeScript](https://img.shields.io/badge/Code-TypeScript-294E80.svg)](https://www.typescriptlang.org/)
[![pnpm](https://img.shields.io/badge/Package_Manager-pnpm-F69220.svg)](https://pnpm.io/)

This repository is structured as a **Monorepo** managed using **pnpm Workspaces**. It separates the application into distinct, independently deployable packages to promote code reuse, consistency, and efficient dependency management.

## 🛠️ Tech Stack Overview

| Area | Package | Technology | Description |
| :--- | :--- | :--- | :--- |
| **Backend** | `backend` | **NestJS**, TypeScript | REST API built on Node.js, providing core application logic and data access. |
| **Frontend** | `frontend` | **React**, **Vite**, TypeScript | Single Page Application (SPA) for the user interface. |
| **Shared** | `shared/types` | TypeScript | Contains common interfaces, DTOs (Data Transfer Objects), and utility types used across all packages. |

## 📦 Monorepo Structure
```
.
├── backend/            # NestJS API package (e.g., @project/api)
├── frontend/           # React/Vite UI package (e.g., @project/frontend)
├── shared/
│   └── types/          # Shared TypeScript types package (e.g., @project/types)
├── package.json        # Root workspace configuration
├── pnpm-workspace.yaml # Defines package workspaces
└── tsconfig.json       # Global TypeScript configuration
```

## ⚙️ Getting Started

### Prerequisites

* Node.js (LTS version)
* [pnpm](https://pnpm.io/installation) package manager

### Installation

#### Install all dependencies:
Run this command from the root directory. pnpm will install all project dependencies and correctly symlink local workspace packages (e.g., linking `@project/types` into the API and Frontend).
```
pnpm install
```

## 🚀 Available Scripts

All major operations are executed from the root directory using **pnpm filters (`--filter`)** to target specific packages.

### Development

| Command | Target Package | Description |
| :--- | :--- | :--- |
| `pnpm start:backend` | `@project/backend` | Starts the NestJS backend in watch mode (hot reload). |
| `pnpm start:frontend` | `@project/frontend` | Starts the React development server via Vite. |
| `pnpm dev:all` | Global | **(Custom)** Run both frontend and backend concurrently (Requires `concurrently` or similar setup in root `package.json`). |

### Building and Linting

| Command | Target Package | Description |
| :--- | :--- | :--- |
| `pnpm build:all` | All | Builds all necessary packages (types, then API, then frontend). |
| `pnpm --filter {package} build` | Specific | Builds a single specified package (e.g., `pnpm --filter @project/api build`). |
| `pnpm lint:all` | All | Runs linter across all packages. |
