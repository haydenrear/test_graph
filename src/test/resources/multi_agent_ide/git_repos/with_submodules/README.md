# Repository with Submodules

This repository demonstrates the Multi-Agent IDE with git submodules support.

## Structure

- Main repository with orchestration logic
- auth-lib submodule for authentication utilities
- utils-lib submodule for general utilities

## Submodules

### auth-lib
Authentication library for user management and token validation.

### utils-lib
General utility functions used across the application.

## Building

1. Clone with submodules: `git clone --recursive`
2. Initialize submodules: `git submodule update --init --recursive`
3. Build the application
