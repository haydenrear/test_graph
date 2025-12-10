"""
Execution service for running workflow tasks.
"""


class ExecutionService:
    """Service that executes tasks in the workflow."""

    def __init__(self):
        """Initialize the execution service."""
        self.execution_log = []

    def execute(self, task):
        """
        Execute a task.

        Args:
            task: Task configuration

        Returns:
            Execution result
        """
        task_id = task.get("id", "unknown")
        task_type = task.get("type", "unknown")

        self.execution_log.append(
            {"task_id": task_id, "task_type": task_type, "status": "executed"}
        )

        return {
            "task_id": task_id,
            "status": "success",
            "output": self._process_task(task),
        }

    def _process_task(self, task):
        """Process the task and return output."""
        task_type = task.get("type")

        if task_type == "code_generation":
            return self._generate_code(task)
        elif task_type == "testing":
            return self._run_tests(task)
        elif task_type == "merge":
            return self._merge_changes(task)

        return {"message": f"Task type {task_type} processed"}

    def _generate_code(self, task):
        """Generate code for the task."""
        return {"code": "# Generated code", "status": "generated"}

    def _run_tests(self, task):
        """Run tests for the task."""
        return {"tests_passed": 10, "tests_failed": 0, "status": "passed"}

    def _merge_changes(self, task):
        """Merge changes for the task."""
        return {"merged": True, "conflicts": 0, "status": "merged"}

    def get_execution_log(self):
        """Get the execution log."""
        return self.execution_log
