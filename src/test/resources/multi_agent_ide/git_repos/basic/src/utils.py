"""
Utility functions for the basic test repository.
"""


def format_output(data):
    """Format output data as string."""
    return str(data)


def validate_input(input_data):
    """Validate input data."""
    return input_data is not None and len(str(input_data)) > 0
