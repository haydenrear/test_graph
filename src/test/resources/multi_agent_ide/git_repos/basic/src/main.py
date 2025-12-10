#!/usr/bin/env python3
"""
Main application entry point for the basic test repository.
"""

def greet(name):
    """Greet a person by name."""
    return f"Hello, {name}!"

def main():
    """Main function."""
    print(greet("World"))
    print("Basic repository initialized successfully!")

if __name__ == "__main__":
    main()
