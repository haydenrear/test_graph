"""
General utility functions used across the application.
"""


def format_output(data):
    """
    Format output data as string.

    Args:
        data: Data to format

    Returns:
        String representation of data
    """
    if isinstance(data, bool):
        return "true" if data else "false"
    return str(data)


def validate_input(input_data):
    """
    Validate input data.

    Args:
        input_data: Data to validate

    Returns:
        True if input is valid, False otherwise
    """
    if input_data is None:
        return False

    input_str = str(input_data).strip()
    return len(input_str) > 0


def merge_dicts(dict1, dict2):
    """
    Merge two dictionaries.

    Args:
        dict1: First dictionary
        dict2: Second dictionary

    Returns:
        Merged dictionary
    """
    result = dict1.copy()
    result.update(dict2)
    return result
