"""
Orchestration logic for complex workflows.
"""


class Orchestrator:
    """Coordinates execution of multiple agents and services."""

    def __init__(self):
        """Initialize the orchestrator."""
        self.agents = {}
        self.services = {}
        self.workflow_state = {}

    def register_agent(self, agent_id, agent):
        """Register an agent with the orchestrator."""
        self.agents[agent_id] = agent

    def register_service(self, service_id, service):
        """Register a service with the orchestrator."""
        self.services[service_id] = service

    def execute_workflow(self, workflow_id, tasks):
        """
        Execute a workflow with multiple tasks.

        Args:
            workflow_id: Unique identifier for the workflow
            tasks: List of tasks to execute

        Returns:
            Workflow execution result
        """
        self.workflow_state[workflow_id] = {
            "status": "running",
            "tasks_completed": 0,
            "tasks_total": len(tasks),
        }

        results = []
        for task in tasks:
            result = self._execute_task(workflow_id, task)
            results.append(result)
            self.workflow_state[workflow_id]["tasks_completed"] += 1

        self.workflow_state[workflow_id]["status"] = "completed"
        return results

    def _execute_task(self, workflow_id, task):
        """Execute a single task."""
        task_type = task.get("type")
        agent_id = task.get("agent")

        if agent_id in self.agents:
            return self.agents[agent_id].execute(task)

        return {"status": "failed", "reason": f"Agent {agent_id} not found"}

    def get_workflow_status(self, workflow_id):
        """Get the status of a workflow."""
        return self.workflow_state.get(workflow_id, {})
