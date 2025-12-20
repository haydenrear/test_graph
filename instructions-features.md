# Overview

Test graph is a composable but decoupled computation graph over domain events across the application architecture. By
decoupling the state from the activities, we can both make each change independently testable, but also test 
integration. See instructions.md for more information about how the test graph framework works and how to hook into
it.

## Requirements

### How To Think About Integration Testing with Test Graph

It is very important at the layer of integration test to only test the communication of the domain model across the 
application boundary. Therefore, most, if not all, of our feature files can be implemented as assertions over events
fired from the applications. Whether that be selenium events that we listen to on the UI, or events on the backend,
we should not be testing the internals of the application. Instead, only the behavior, and only use the required patterns 
of the test_graph below.

### Required Pattern for Gherkin and Cucumber with Test Graph

When writing the feature files, maintainability, brevity, and simplicity are most important. The overall pattern is

1. Initialize state
2. Setup listeners
3. Assert events

We'd like to minimize the number of step definitions to just a few, and be templated. For example, no step definition
should only be used for that scenario. All step definitions should be generic, and all then clauses should be the assertion
that an event was received. Similarly, the initialization of the state should be as general as possible so we can 
reuse it. In this way, we minimize the number of step definitions.

It should be easy to minimize the number of step definitions in this way, because we're translating the acceptance 
criteria into initialization, setup, and assertion of events. Then, in the step definitions, we'll be setting the 
values of our context objects and adding assertions to the assertion context. Once we finish with context setup, a final 
step definition such as, and all assertion events were received will be executed. That is when all assertions set in 
the context will be asserted from the event listeners that were set up.

By defining it as a state and then actions over the state, we can minimize the number of step definitions and simplify
the testing of the contracts. Additionally, because the cucumber tags match with the acceptance criteria in our spec,
we can see just how that contract maps to events, which are communication of our domain model across application 
boundaries.


### Invariants

So it can be easy to include in the given a state that cannot be created by simply calling the service, but this is wrong. For instance, if I have a computation graph in the service of the memory I test, it would be incorrect to say given the state of that computation graph is such. Instead, the givens must be a state that can be created using the interfaces provided by the service, and typically this means rest endpoints or websocket endpoints.

Instead, we provide the starting state for the service that can be produced by calling the service, and then we mock out the service's dependencies that are required to produce the state in memory that cannot be produced explicitly. We do this using mountbank imposters. You can see in instructions.md more information about how to use mountbank imposters.

Please never assume you can create an arbitrary state that cannot be created by calling the service. Instead, use the provided starting state and mock out the service's dependencies to create the state in memory that cannot be produced explicitly. This may mean mocking out the responses to an LLM call over the network, and then that in-memory state will be produced by the service itself.
