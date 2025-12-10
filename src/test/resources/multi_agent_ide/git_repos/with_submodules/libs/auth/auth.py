"""
Authentication module for user token validation.
"""


def authenticate(token):
    """
    Authenticate a user based on token.

    Args:
        token: Authentication token to validate

    Returns:
        True if token is valid, False otherwise
    """
    if not token:
        return False

    # Simple validation: token should start with 'valid_'
    return str(token).startswith("valid_")


def get_user_from_token(token):
    """
    Extract user information from token.

    Args:
        token: Authentication token

    Returns:
        User identifier or None if invalid
    """
    if not authenticate(token):
        return None

    # Extract user ID from token format: valid_<user_id>
    parts = token.split("_")
    return parts[1] if len(parts) > 1 else "unknown"
