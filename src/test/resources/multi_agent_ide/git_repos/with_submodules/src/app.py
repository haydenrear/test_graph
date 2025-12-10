#!/usr/bin/env python3
"""
Main application entry point with submodule integration.
"""

import sys

sys.path.insert(0, "libs/auth")
sys.path.insert(0, "libs/utils")

try:
    from auth import authenticate
    from utils import format_output
except ImportError as e:
    print(f"Warning: Could not import submodules: {e}")
    authenticate = None
    format_output = None


def main():
    """Main function demonstrating submodule usage."""
    print("Multi-Agent IDE Application with Submodules")
    print("=" * 50)

    if authenticate:
        token = "valid_test_token"
        is_valid = authenticate(token)
        print(f"Authentication check: {format_output(is_valid)}")

    print("Application started successfully!")


if __name__ == "__main__":
    main()
